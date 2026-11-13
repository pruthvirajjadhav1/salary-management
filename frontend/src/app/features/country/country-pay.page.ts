import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Select } from 'primeng/select';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { Tooltip } from 'primeng/tooltip';
import { catchError, map, of, startWith, switchMap } from 'rxjs';

import { InsightsApi } from '../../core/api/insights-api';
import { JobTitleBreakdown, JobTitlePay } from '../../core/api/models';
import { formatMoney, toUsd } from '../../core/format/money';
import { BandLegend } from '../../shared/band-legend';
import { MoneyPipe, UsdPipe } from '../../shared/format.pipes';
import { PayBand, sharedDomain } from '../../shared/pay-band';

type ViewState =
  | { state: 'loading' }
  | { state: 'missing' }
  | { state: 'error' }
  | { state: 'loaded'; data: JobTitleBreakdown };

/** Flattened so PrimeNG can sort by any column client-side (the list is a few dozen rows). */
interface JobRow extends JobTitlePay {
  headcount: number;
  median: number;
  average: number;
  min: number;
  max: number;
  tooltip: string;
}

@Component({
  selector: 'app-country-pay-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, FormsModule, TableModule, Select, Skeleton, Tooltip, PayBand, BandLegend, MoneyPipe, UsdPipe],
  templateUrl: './country-pay.page.html',
  styleUrl: './country-pay.page.scss',
})
export class CountryPayPage {
  private readonly insights = inject(InsightsApi);
  private readonly router = inject(Router);

  /** Bound from the `:countryCode` route parameter. */
  readonly countryCode = input.required<string>();

  protected readonly department = signal<string | null>(null);

  protected readonly view = toSignal(
    toObservable(this.countryCode).pipe(
      switchMap((code) =>
        this.insights.jobTitles(code).pipe(
          map((data): ViewState => ({ state: 'loaded', data })),
          catchError((error) => of<ViewState>({ state: error?.status === 404 ? 'missing' : 'error' })),
          startWith<ViewState>({ state: 'loading' }),
        ),
      ),
    ),
    { initialValue: { state: 'loading' } as ViewState },
  );

  protected readonly data = computed(() => {
    const view = this.view();
    return view.state === 'loaded' ? view.data : null;
  });

  protected readonly currency = computed(() => this.data()?.country.currencyCode ?? 'USD');

  protected readonly departments = computed(() =>
    [...new Set(this.data()?.jobTitles.map((j) => j.department) ?? [])].sort(),
  );

  protected readonly rows = computed<JobRow[]>(() => {
    const department = this.department();
    const currency = this.currency();
    return (this.data()?.jobTitles ?? [])
      .filter((job) => !department || job.department === department)
      .map((job) => ({
        ...job,
        headcount: job.salary.headcount,
        median: job.salary.median,
        average: job.salary.average,
        min: job.salary.min,
        max: job.salary.max,
        tooltip: `Min ${formatMoney(job.salary.min, currency)}  ·  Median ${formatMoney(job.salary.median, currency)}  ·  Max ${formatMoney(job.salary.max, currency)}`,
      }));
  });

  /** One scale for the whole country, so bands stay comparable when a department filter is applied. */
  protected readonly domain = computed(() => sharedDomain(this.data()?.jobTitles.map((j) => j.salary) ?? []));

  protected readonly payrollUsd = computed(() => {
    const data = this.data();
    return data?.overall ? toUsd(data.overall.total, data.country.unitsPerUsd) : null;
  });

  protected readonly skeletonRows = Array.from({ length: 10 });

  protected openPeople(job: JobRow): void {
    this.router.navigate(['/people'], { queryParams: { country: this.countryCode().toUpperCase(), jobTitle: job.jobTitle } });
  }
}
