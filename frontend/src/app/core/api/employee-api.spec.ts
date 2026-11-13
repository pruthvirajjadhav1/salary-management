import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { EmployeeApi, toParams } from './employee-api';

describe('EmployeeApi', () => {
  let api: EmployeeApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(EmployeeApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only the filters that are set', () => {
    api.search({ q: ' patel ', country: 'IN', department: null, jobTitle: '', page: 2, size: 25, sort: 'salary,desc' }).subscribe();

    const request = http.expectOne((r) => r.url === '/api/employees');
    expect(request.request.params.keys().sort()).toEqual(['country', 'page', 'q', 'size', 'sort']);
    expect(request.request.params.get('q')).toBe('patel');
    request.flush({ items: [], page: 2, size: 25, totalItems: 0, totalPages: 0 });
  });

  it('exports with the same filters and order as the screen, but every page', () => {
    expect(api.exportUrl({ country: 'DE', sort: 'name,asc', page: 3, size: 50 })).toBe('/api/employees/export?country=DE&sort=name,asc');
    expect(api.exportUrl({})).toBe('/api/employees/export');
  });

  it('updates an employee with PUT', () => {
    api
      .update(7, {
        fullName: 'Asha Rao',
        email: 'asha@acme.example',
        jobTitle: 'Analyst',
        department: 'Finance',
        countryCode: 'IN',
        annualSalary: 1800000,
        hireDate: '2022-04-01',
      })
      .subscribe();

    const request = http.expectOne('/api/employees/7');
    expect(request.request.method).toBe('PUT');
    request.flush({});
  });

  it('keeps zero as a real value when building params', () => {
    expect(toParams({ page: 0 }).get('page')).toBe('0');
  });
});
