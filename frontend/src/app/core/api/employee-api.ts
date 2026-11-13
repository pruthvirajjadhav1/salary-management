import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { EmployeeDetails, EmployeeQuery, EmployeeRequest, EmployeeSummary, Page } from './models';

@Injectable({ providedIn: 'root' })
export class EmployeeApi {
  private readonly http = inject(HttpClient);

  search(query: EmployeeQuery): Observable<Page<EmployeeSummary>> {
    return this.http.get<Page<EmployeeSummary>>('/api/employees', { params: toParams(query) });
  }

  /** Same filters and order as the directory, so the file matches what is on screen. */
  exportUrl(query: EmployeeQuery): string {
    const params = toParams({ ...query, page: undefined, size: undefined }).toString();
    return '/api/employees/export' + (params ? `?${params}` : '');
  }

  get(id: number): Observable<EmployeeDetails> {
    return this.http.get<EmployeeDetails>(`/api/employees/${id}`);
  }

  create(request: EmployeeRequest): Observable<EmployeeDetails> {
    return this.http.post<EmployeeDetails>('/api/employees', request);
  }

  update(id: number, request: EmployeeRequest): Observable<EmployeeDetails> {
    return this.http.put<EmployeeDetails>(`/api/employees/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/employees/${id}`);
  }
}

/** Drops empty values so the URL only carries filters that are actually set. */
export function toParams(query: EmployeeQuery): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== null && value !== undefined && `${value}`.trim() !== '') {
      params = params.set(key, `${value}`.trim());
    }
  }
  return params;
}
