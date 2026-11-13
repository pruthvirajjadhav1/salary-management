/** Mirrors the backend's JSON. Money is always whole units of `currencyCode` unless the name ends in `Usd`. */

export interface Country {
  code: string;
  name: string;
  currencyCode: string;
  unitsPerUsd: number;
  rateAsOf: string;
}

export interface ReferenceData {
  countries: Country[];
  departments: string[];
  jobTitles: string[];
}

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface EmployeeSummary {
  id: number;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  countryCode: string;
  countryName: string;
  currencyCode: string;
  annualSalary: number;
  annualSalaryUsd: number;
  hireDate: string;
}

export interface SalaryChange {
  id: number;
  annualSalary: number;
  currencyCode: string;
  effectiveDate: string;
  reason: string | null;
  recordedAt: string;
}

export interface SalaryStatistics {
  headcount: number;
  min: number;
  p25: number;
  median: number;
  average: number;
  p75: number;
  max: number;
  total: number;
}

export type PayBand = 'BELOW' | 'WITHIN' | 'ABOVE';

export interface PeerComparison {
  jobTitle: string;
  countryCode: string;
  currencyCode: string;
  peers: SalaryStatistics;
  position: { compaRatio: number; band: PayBand };
}

export interface EmployeeDetails extends EmployeeSummary {
  createdAt: string;
  updatedAt: string;
  salaryHistory: SalaryChange[];
  peerComparison: PeerComparison | null;
}

export interface EmployeeRequest {
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  countryCode: string;
  annualSalary: number | null;
  hireDate: string | null;
  salaryEffectiveDate?: string | null;
  salaryChangeReason?: string | null;
}

export interface EmployeeQuery {
  q?: string | null;
  country?: string | null;
  department?: string | null;
  jobTitle?: string | null;
  sort?: string | null;
  page?: number;
  size?: number;
}

export interface DepartmentPay {
  department: string;
  headcount: number;
  totalPayrollUsd: number;
  medianSalaryUsd: number;
}

export interface OrgOverview {
  headcount: number;
  countryCount: number;
  totalPayrollUsd: number;
  medianSalaryUsd: number | null;
  averageSalaryUsd: number | null;
  ratesAsOf: string | null;
  departments: DepartmentPay[];
}

export interface CountryPay {
  countryCode: string;
  countryName: string;
  currencyCode: string;
  salary: SalaryStatistics;
  medianSalaryUsd: number;
  totalPayrollUsd: number;
}

export interface JobTitlePay {
  jobTitle: string;
  department: string;
  salary: SalaryStatistics;
}

export interface JobTitleBreakdown {
  country: Country;
  overall: SalaryStatistics | null;
  jobTitles: JobTitlePay[];
}

/** RFC 9457 problem detail, with the backend's `errors` extension (field -> message). */
export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string>;
}
