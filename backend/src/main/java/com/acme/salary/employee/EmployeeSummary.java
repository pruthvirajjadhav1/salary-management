package com.acme.salary.employee;

import java.time.LocalDate;

/** One row of the directory: salary in local currency plus its USD equivalent for comparison. */
public record EmployeeSummary(long id, String fullName, String email, String jobTitle, String department,
		String countryCode, String countryName, String currencyCode, long annualSalary, long annualSalaryUsd,
		LocalDate hireDate) {
}
