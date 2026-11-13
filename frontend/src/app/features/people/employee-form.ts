import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { startWith } from 'rxjs';

import { Country, EmployeeDetails, EmployeeRequest } from '../../core/api/models';
import { fromIsoDate, toIsoDate } from '../../core/format/dates';
import { currencySymbol, formatChange, formatMoney } from '../../core/format/money';

export const REASON_SUGGESTIONS = ['Annual review', 'Promotion', 'Market adjustment', 'Relocation', 'Correction'];

const MESSAGES: Record<string, (error: unknown) => string> = {
  required: () => 'Required',
  email: () => 'Enter a valid email address',
  maxlength: (e) => `Keep it under ${(e as { requiredLength: number }).requiredLength} characters`,
  min: () => 'Must be more than zero',
  max: () => 'That is more than any salary we expect',
  server: (e) => `${e}`,
};

/** Create or edit an employee. When an edit changes pay, it asks when the change takes effect and why. */
@Component({
  selector: 'app-employee-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, InputText, InputNumber, Select, DatePicker, AutoComplete, Button],
  templateUrl: './employee-form.html',
  styleUrl: './employee-form.scss',
})
export class EmployeeForm {
  private readonly fb = inject(FormBuilder).nonNullable;

  /** The employee being edited, or null to create one. */
  readonly employee = input<EmployeeDetails | null>(null);
  readonly countries = input<Country[]>([]);
  readonly departments = input<string[]>([]);
  readonly jobTitles = input<string[]>([]);
  readonly saving = input(false);
  readonly serverErrors = input<Record<string, string>>({});

  readonly save = output<EmployeeRequest>();
  readonly cancel = output<void>();

  protected readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    jobTitle: ['', [Validators.required, Validators.maxLength(80)]],
    department: ['', [Validators.required, Validators.maxLength(80)]],
    countryCode: ['', Validators.required],
    annualSalary: this.fb.control<number | null>(null, [Validators.required, Validators.min(1), Validators.max(1_000_000_000)]),
    hireDate: this.fb.control<Date | null>(null, Validators.required),
    salaryEffectiveDate: this.fb.control<Date | null>(new Date()),
    salaryChangeReason: ['', Validators.maxLength(200)],
  });

  private readonly value = toSignal(this.form.valueChanges.pipe(startWith(this.form.getRawValue())), {
    initialValue: this.form.getRawValue(),
  });

  protected readonly submitted = signal(false);
  private readonly lastSubmitted = signal<ReturnType<typeof this.form.getRawValue> | null>(null);
  protected readonly titleSuggestions = signal<string[]>([]);
  protected readonly departmentSuggestions = signal<string[]>([]);
  protected readonly reasons = REASON_SUGGESTIONS;

  protected readonly isEdit = computed(() => this.employee() !== null);
  protected readonly selectedCountry = computed(() => this.countries().find((c) => c.code === this.value().countryCode));
  protected readonly currency = computed(() => this.selectedCountry()?.currencyCode ?? null);
  protected readonly salaryPrefix = computed(() => (this.currency() ? currencySymbol(this.currency()!) + ' ' : ''));

  /** True when an edit changes the salary or moves the person to another country (and so another currency). */
  protected readonly payChanged = computed(() => {
    const original = this.employee();
    const value = this.value();
    return !!original && (value.annualSalary !== original.annualSalary || value.countryCode !== original.countryCode);
  });

  protected readonly payChangeSummary = computed(() => {
    const original = this.employee();
    const value = this.value();
    if (!original || !this.payChanged() || !value.annualSalary || !this.currency()) {
      return '';
    }
    const from = formatMoney(original.annualSalary, original.currencyCode);
    const to = formatMoney(value.annualSalary, this.currency()!);
    const change = original.currencyCode === this.currency() ? ` (${formatChange(original.annualSalary, value.annualSalary)})` : '';
    return `${from} → ${to}${change}`;
  });

  constructor() {
    effect(() => {
      const employee = this.employee();
      this.submitted.set(false);
      this.lastSubmitted.set(null);
      this.form.reset({
        fullName: employee?.fullName ?? '',
        email: employee?.email ?? '',
        jobTitle: employee?.jobTitle ?? '',
        department: employee?.department ?? '',
        countryCode: employee?.countryCode ?? '',
        annualSalary: employee?.annualSalary ?? null,
        hireDate: fromIsoDate(employee?.hireDate),
        salaryEffectiveDate: new Date(),
        salaryChangeReason: '',
      });
    });

  }

  /**
   * Client-side problems first; otherwise what the server said about this field, for as long as the field
   * still holds the value that was submitted. Reads signals, so a late server response re-renders the form.
   */
  protected errorFor(name: string): string | null {
    const control: AbstractControl | null = this.form.get(name);
    if (control?.errors && (control.touched || this.submitted())) {
      const [key, detail] = Object.entries(control.errors)[0];
      return MESSAGES[key]?.(detail) ?? 'Invalid value';
    }
    const serverMessage = this.serverErrors()[name];
    const submitted = this.lastSubmitted() as Record<string, unknown> | null;
    const current = this.value() as Record<string, unknown>;
    return serverMessage && submitted && submitted[name] === current[name] ? serverMessage : null;
  }

  protected suggestTitles(event: AutoCompleteCompleteEvent): void {
    this.titleSuggestions.set(matching(this.jobTitles(), event.query));
  }

  protected suggestDepartments(event: AutoCompleteCompleteEvent): void {
    this.departmentSuggestions.set(matching(this.departments(), event.query));
  }

  protected useReason(reason: string): void {
    this.form.controls.salaryChangeReason.setValue(reason);
  }

  protected submit(): void {
    this.submitted.set(true);
    if (this.payChanged() && !this.form.controls.salaryEffectiveDate.value) {
      this.form.controls.salaryEffectiveDate.setErrors({ required: true });
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.lastSubmitted.set(v);
    this.save.emit({
      fullName: v.fullName,
      email: v.email,
      jobTitle: v.jobTitle,
      department: v.department,
      countryCode: v.countryCode,
      annualSalary: v.annualSalary,
      hireDate: toIsoDate(v.hireDate),
      salaryEffectiveDate: this.payChanged() ? toIsoDate(v.salaryEffectiveDate) : null,
      salaryChangeReason: this.payChanged() ? v.salaryChangeReason || null : null,
    });
  }
}

/** Case-insensitive contains-match; an empty query lists everything (the dropdown button). */
export function matching(options: readonly string[], query: string): string[] {
  const needle = query.trim().toLowerCase();
  return options.filter((option) => option.toLowerCase().includes(needle));
}
