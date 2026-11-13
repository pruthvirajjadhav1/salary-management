import { ComponentFixture, TestBed } from '@angular/core/testing';
import { providePrimeNG } from 'primeng/config';
import { beforeEach, describe, expect, it } from 'vitest';

import { Country, EmployeeDetails, EmployeeRequest } from '../../core/api/models';
import { EmployeeForm, matching } from './employee-form';

const COUNTRIES: Country[] = [
  { code: 'US', name: 'United States', currencyCode: 'USD', unitsPerUsd: 1, rateAsOf: '2026-11-01' },
  { code: 'GB', name: 'United Kingdom', currencyCode: 'GBP', unitsPerUsd: 0.79, rateAsOf: '2026-11-01' },
];

const ASHA: EmployeeDetails = {
  id: 7,
  fullName: 'Asha Rao',
  email: 'asha@acme.example',
  jobTitle: 'Software Engineer',
  department: 'Engineering',
  countryCode: 'US',
  countryName: 'United States',
  currencyCode: 'USD',
  annualSalary: 100000,
  annualSalaryUsd: 100000,
  hireDate: '2022-04-01',
  createdAt: '2022-04-01T00:00:00Z',
  updatedAt: '2022-04-01T00:00:00Z',
  salaryHistory: [],
  peerComparison: null,
};

describe('EmployeeForm', () => {
  let fixture: ComponentFixture<EmployeeForm>;
  let emitted: EmployeeRequest[];

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [providePrimeNG()] });
    fixture = TestBed.createComponent(EmployeeForm);
    fixture.componentRef.setInput('countries', COUNTRIES);
    emitted = [];
    fixture.componentInstance.save.subscribe((request) => emitted.push(request));
  });

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const form = () => (fixture.componentInstance as any).form;
  const text = () => (fixture.nativeElement as HTMLElement).textContent ?? '';
  const submit = () => {
    (fixture.nativeElement as HTMLElement).querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  };

  it('does not submit an empty form and says what is missing', () => {
    fixture.detectChanges();

    submit();

    expect(emitted).toEqual([]);
    expect(text()).toContain('Required');
  });

  it('asks for an effective date and reason only when an edit changes pay', () => {
    fixture.componentRef.setInput('employee', ASHA);
    fixture.detectChanges();
    expect(text()).not.toContain('This is a pay change');

    form().controls.annualSalary.setValue(112000);
    fixture.detectChanges();

    expect(text()).toContain('This is a pay change: $100,000 → $112,000 (+12.0%)');
  });

  it('treats a move to another country as a pay change, even at the same number', () => {
    fixture.componentRef.setInput('employee', ASHA);
    fixture.detectChanges();

    form().controls.countryCode.setValue('GB');
    fixture.detectChanges();

    expect(text()).toContain('This is a pay change: $100,000 → £100,000');
  });

  it('sends the pay change with its effective date and reason', () => {
    fixture.componentRef.setInput('employee', ASHA);
    fixture.detectChanges();
    form().controls.annualSalary.setValue(112000);
    form().controls.salaryEffectiveDate.setValue(new Date(2026, 0, 1));
    form().controls.salaryChangeReason.setValue('Annual review');

    submit();

    expect(emitted).toHaveLength(1);
    expect(emitted[0]).toMatchObject({
      annualSalary: 112000,
      hireDate: '2022-04-01',
      salaryEffectiveDate: '2026-01-01',
      salaryChangeReason: 'Annual review',
    });
  });

  it('does not send salary-change fields when pay is unchanged', () => {
    fixture.componentRef.setInput('employee', ASHA);
    fixture.detectChanges();
    form().controls.fullName.setValue('Asha Rao-Menon');

    submit();

    expect(emitted[0]).toMatchObject({ fullName: 'Asha Rao-Menon', salaryEffectiveDate: null, salaryChangeReason: null });
  });

  it('shows an error the server reports next to the right field until that field is changed', () => {
    fixture.componentRef.setInput('employee', ASHA);
    fixture.detectChanges();
    submit();

    fixture.componentRef.setInput('serverErrors', { email: 'is already used by another employee' });
    fixture.detectChanges();
    expect(text()).toContain('is already used by another employee');

    form().controls.email.setValue('asha.rao@acme.example');
    fixture.detectChanges();
    expect(text()).not.toContain('is already used by another employee');
  });
});

describe('suggestions', () => {
  it('match anywhere in the option, ignoring case', () => {
    expect(matching(['Software Engineer', 'Staff Engineer', 'Accountant'], 'ENGI')).toEqual(['Software Engineer', 'Staff Engineer']);
    expect(matching(['A', 'B'], '')).toEqual(['A', 'B']);
  });
});
