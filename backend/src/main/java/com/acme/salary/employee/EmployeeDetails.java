package com.acme.salary.employee;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.acme.salary.insights.InsightsService.PeerComparison;

/** Everything the employee profile shows: current record, salary history and pay against peers. */
public record EmployeeDetails(long id, String fullName, String email, String jobTitle, String department,
		String countryCode, String countryName, String currencyCode, long annualSalary, long annualSalaryUsd,
		LocalDate hireDate, Instant createdAt, Instant updatedAt, List<SalaryChange> salaryHistory,
		PeerComparison peerComparison) {

	public record SalaryChange(long id, long annualSalary, String currencyCode, LocalDate effectiveDate,
			String reason, Instant recordedAt) {
	}

}
