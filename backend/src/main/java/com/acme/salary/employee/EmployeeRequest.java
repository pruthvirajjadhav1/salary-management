package com.acme.salary.employee;

import java.time.LocalDate;
import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Create or update an employee. {@code salaryEffectiveDate} and {@code salaryChangeReason} are only used
 * when an update changes the salary or country; the effective date defaults to today.
 */
public record EmployeeRequest(
		@NotBlank @Size(max = 120) String fullName,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(max = 80) String jobTitle,
		@NotBlank @Size(max = 80) String department,
		@NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "must be a two-letter country code") String countryCode,
		@NotNull @Positive @Max(1_000_000_000) Long annualSalary,
		@NotNull LocalDate hireDate,
		LocalDate salaryEffectiveDate,
		@Size(max = 200) String salaryChangeReason) {

	/** Trims text, lower-cases the email and upper-cases the country code so stored data stays consistent. */
	EmployeeRequest normalized() {
		return new EmployeeRequest(fullName.strip(), email.strip().toLowerCase(Locale.ROOT), jobTitle.strip(),
				department.strip(), countryCode.strip().toUpperCase(Locale.ROOT), annualSalary, hireDate,
				salaryEffectiveDate, blankToNull(salaryChangeReason));
	}

	private static String blankToNull(String value) {
		return (value == null || value.isBlank()) ? null : value.strip();
	}

}
