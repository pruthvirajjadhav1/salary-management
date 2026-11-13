import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { Tooltip } from 'primeng/tooltip';
import { catchError, forkJoin, map, of, startWith } from 'rxjs';

import { InsightsApi, ReferenceApi } from '../../core/api/insights-api';
import { CountryPay, OrgOverview } from '../../core/api/models';
import { formatDate } from '../../core/format/dates';
import { formatMoney, toUsd } from '../../core/format/money';
import { BandLegend } from '../../shared/band-legend';
import { MoneyPipe, UsdPipe } from '../../shared/format.pipes';
import { BandStats, PayBand, sharedDomain } from '../../shared/pay-band';

interface CountryRow extends CountryPay {
  usdBand: BandStats;
  tooltip: string;
}

interface Loaded {
  state: 'loaded';
  overview: OrgOverview;
  countries: CountryRow[];
}

type ViewState = { state: 'loading' } | { state: 'error' } | Loaded;

@Component({
  selector: 'app-overview-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TableModule, Skeleton, Tooltip, PayBand, BandLegend, MoneyPipe, UsdPipe],
  templateUrl: './overview.page.html',
  styleUrl: './overview.page.scss',
})
export class OverviewPage {
  private readonly insights = inject(InsightsApi);
  private readonly reference = inject(ReferenceApi);
  private readonly router = inject(Router);

  protected readonly view = toSignal(
    forkJoin({
      overview: this.insights.overview(),
      countries: this.insights.countries(),
      reference: this.reference.data(),
    }).pipe(
      map(({ overview, countries, reference }): ViewState => {
        const rates = new Map(reference.countries.map((c) => [c.code, c.unitsPerUsd]));
        const rows = countries.map((country): CountryRow => {
          const rate = rates.get(country.countryCode) ?? 1;
          const s = country.salary;
          return {
            ...country,
            usdBand: {
              min: toUsd(s.min, rate),
              p25: toUsd(s.p25, rate),
              median: toUsd(s.median, rate),
              p75: toUsd(s.p75, rate),
              max: toUsd(s.max, rate),
            },
            tooltip: bandTooltip(s, country.currencyCode),
          };
        });
        return { state: 'loaded', overview, countries: rows };
      }),
      catchError(() => of<ViewState>({ state: 'error' })),
      startWith<ViewState>({ state: 'loading' }),
    ),
    { requireSync: true },
  );

  protected readonly loaded = computed(() => {
    const view = this.view();
    return view.state === 'loaded' ? view : null;
  });

  protected readonly domain = computed(() => sharedDomain(this.loaded()?.countries.map((c) => c.usdBand) ?? []));

  protected readonly topPayroll = computed(() =>
    Math.max(1, ...(this.loaded()?.overview.departments.map((d) => d.totalPayrollUsd) ?? [])),
  );

  protected readonly ratesAsOf = computed(() => formatDate(this.loaded()?.overview.ratesAsOf));

  protected readonly skeletonRows = Array.from({ length: 8 });

  protected openCountry(code: string): void {
    this.router.navigate(['/overview', code]);
  }

  protected share(payroll: number): number {
    const total = this.loaded()?.overview.totalPayrollUsd ?? 0;
    return total > 0 ? (payroll / total) * 100 : 0;
  }

  protected barWidth(payroll: number): number {
    return (payroll / this.topPayroll()) * 100;
  }
}

function bandTooltip(s: CountryPay['salary'], currency: string): string {
  return [
    `Min ${formatMoney(s.min, currency)}`,
    `P25 ${formatMoney(s.p25, currency)}`,
    `Median ${formatMoney(s.median, currency)}`,
    `P75 ${formatMoney(s.p75, currency)}`,
    `Max ${formatMoney(s.max, currency)}`,
  ].join('  ·  ');
}
