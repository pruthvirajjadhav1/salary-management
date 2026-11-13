import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { CountryPay, JobTitleBreakdown, OrgOverview, ReferenceData } from './models';

@Injectable({ providedIn: 'root' })
export class InsightsApi {
  private readonly http = inject(HttpClient);

  overview(): Observable<OrgOverview> {
    return this.http.get<OrgOverview>('/api/insights/overview');
  }

  countries(): Observable<CountryPay[]> {
    return this.http.get<CountryPay[]>('/api/insights/countries');
  }

  jobTitles(countryCode: string): Observable<JobTitleBreakdown> {
    return this.http.get<JobTitleBreakdown>(`/api/insights/countries/${encodeURIComponent(countryCode)}/job-titles`);
  }
}

@Injectable({ providedIn: 'root' })
export class ReferenceApi {
  private readonly http = inject(HttpClient);
  private cached$?: Observable<ReferenceData>;

  /** Cached for the session; call `refresh()` after a change that may add a department or job title. */
  data(): Observable<ReferenceData> {
    this.cached$ ??= this.http.get<ReferenceData>('/api/reference').pipe(shareReplay(1));
    return this.cached$;
  }

  refresh(): void {
    this.cached$ = undefined;
  }
}
