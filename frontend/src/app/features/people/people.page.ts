import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Button } from 'primeng/button';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { BehaviorSubject, Subject, catchError, combineLatest, debounceTime, distinctUntilChanged, map, of, switchMap, tap } from 'rxjs';

import { EmployeeApi } from '../../core/api/employee-api';
import { ReferenceApi } from '../../core/api/insights-api';
import { EmployeeDetails, EmployeeQuery, EmployeeSummary, Page } from '../../core/api/models';
import { DayPipe, MoneyPipe, UsdPipe } from '../../shared/format.pipes';
import { InitialsAvatar } from '../../shared/initials-avatar';
import { EmployeePanel } from './employee-panel';

export const DEFAULT_PAGE_SIZE = 25;
const DEFAULT_SORT = 'name,asc';

export interface DirectoryQuery extends EmployeeQuery {
  sort: string;
  page: number;
  size: number;
}

/** Reads the directory state from the URL, so filters, sorting, paging and the open profile survive reloads and can be shared. */
export function queryFromParams(params: Params): DirectoryQuery {
  const page = Number(params['page'] ?? 0);
  const size = Number(params['size'] ?? DEFAULT_PAGE_SIZE);
  return {
    q: params['q'] ?? null,
    country: params['country'] ?? null,
    department: params['department'] ?? null,
    jobTitle: params['jobTitle'] ?? null,
    sort: params['sort'] ?? DEFAULT_SORT,
    page: Number.isInteger(page) && page >= 0 ? page : 0,
    size: [25, 50, 100].includes(size) ? size : DEFAULT_PAGE_SIZE,
  };
}

type Results = { status: 'loading' | 'loaded' | 'error'; page: Page<EmployeeSummary> | null };

@Component({
  selector: 'app-people-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule, TableModule, Select, Button, IconField, InputIcon, InputText,
    InitialsAvatar, EmployeePanel, MoneyPipe, UsdPipe, DayPipe,
  ],
  templateUrl: './people.page.html',
  styleUrl: './people.page.scss',
  host: { '(document:keydown)': 'onKeydown($event)' },
})
export class PeoplePage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly employees = inject(EmployeeApi);
  private readonly destroyRef = inject(DestroyRef);

  private readonly searchBox = viewChild<ElementRef<HTMLInputElement>>('searchBox');
  private readonly params = toSignal(this.route.queryParams, { requireSync: true });
  private readonly reload$ = new BehaviorSubject<void>(undefined);
  private readonly searchInput$ = new Subject<string>();

  protected readonly reference = toSignal(inject(ReferenceApi).data());
  protected readonly query = computed(() => queryFromParams(this.params()));
  protected readonly searchText = signal(this.query().q ?? '');
  protected readonly creating = signal(false);
  protected readonly openEmployeeId = computed(() => {
    const id = Number(this.params()['employee']);
    return Number.isInteger(id) && id > 0 ? id : null;
  });

  protected readonly results = toSignal(
    combineLatest([toObservable(this.query), this.reload$]).pipe(
      switchMap(([query]) =>
        this.employees.search(query).pipe(
          map((page): Results => ({ status: 'loaded', page })),
          catchError(() => of<Results>({ status: 'error', page: null })),
        ),
      ),
    ),
    { initialValue: { status: 'loading', page: null } as Results },
  );

  protected readonly loading = signal(true);
  protected readonly rows = computed(() => this.results().page?.items ?? []);
  protected readonly total = computed(() => this.results().page?.totalItems ?? 0);
  protected readonly sortField = computed(() => this.query().sort.split(',')[0]);
  protected readonly sortOrder = computed(() => (this.query().sort.split(',')[1] === 'desc' ? -1 : 1));
  protected readonly hasFilters = computed(() => {
    const q = this.query();
    return !!(q.q || q.country || q.department || q.jobTitle);
  });
  protected readonly exportUrl = computed(() => this.employees.exportUrl(this.query()));
  protected readonly countryOptions = computed(() =>
    (this.reference()?.countries ?? []).map((c) => ({ label: c.name, value: c.code })),
  );

  constructor() {
    this.searchInput$
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((q) => this.updateQuery({ q: q.trim() || null }));
    toObservable(this.results)
      .pipe(takeUntilDestroyed())
      .subscribe((r) => this.loading.set(r.status === 'loading'));
    // Show the loading state whenever the query changes, until results arrive.
    toObservable(this.query)
      .pipe(tap(() => this.loading.set(true)), takeUntilDestroyed())
      .subscribe();
  }

  protected onSearch(text: string): void {
    this.searchText.set(text);
    this.searchInput$.next(text);
  }

  protected setFilter(key: 'country' | 'department' | 'jobTitle', value: string | null): void {
    this.updateQuery({ [key]: value || null });
  }

  protected clearFilters(): void {
    this.searchText.set('');
    this.updateQuery({ q: null, country: null, department: null, jobTitle: null });
  }

  /** PrimeNG reports paging and sorting here; we only write it to the URL, which drives the data. */
  protected onLazyLoad(event: TableLazyLoadEvent): void {
    const size = event.rows ?? DEFAULT_PAGE_SIZE;
    const page = Math.floor((event.first ?? 0) / size);
    const field = typeof event.sortField === 'string' ? event.sortField : 'name';
    const sort = `${field},${event.sortOrder === -1 ? 'desc' : 'asc'}`;
    const current = this.query();
    if (page === current.page && size === current.size && sort === current.sort) {
      return;
    }
    const sortChanged = sort !== current.sort || size !== current.size;
    this.navigate({
      page: sortChanged || page === 0 ? null : page,
      size: size === DEFAULT_PAGE_SIZE ? null : size,
      sort: sort === DEFAULT_SORT ? null : sort,
    });
  }

  protected openEmployee(id: number): void {
    this.creating.set(false);
    this.navigate({ employee: id });
  }

  protected startCreate(): void {
    this.navigate({ employee: null });
    this.creating.set(true);
  }

  protected closePanel(): void {
    this.creating.set(false);
    this.navigate({ employee: null });
  }

  protected onSaved(employee: EmployeeDetails): void {
    this.creating.set(false);
    this.navigate({ employee: employee.id });
    this.reload$.next();
  }

  protected onDeleted(): void {
    this.closePanel();
    this.reload$.next();
  }

  protected onKeydown(event: KeyboardEvent): void {
    const target = event.target as HTMLElement | null;
    const typing = target?.closest('input, textarea, [contenteditable="true"], .p-drawer');
    if (event.key === '/' && !typing) {
      event.preventDefault();
      this.searchBox()?.nativeElement.focus();
    }
  }

  private updateQuery(patch: Partial<EmployeeQuery>): void {
    this.navigate({ ...patch, page: null });
  }

  private navigate(queryParams: Params): void {
    this.router.navigate([], { relativeTo: this.route, queryParams, queryParamsHandling: 'merge', replaceUrl: true });
  }
}
