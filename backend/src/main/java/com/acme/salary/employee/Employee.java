package com.acme.salary.employee;

import java.time.Instant;
import java.time.LocalDate;

/** An employee as stored. {@code annualSalary} is the current salary in the country's currency. */
record Employee(long id, String fullName, String email, String jobTitle, String department, String countryCode,
		long annualSalary, LocalDate hireDate, Instant createdAt, Instant updatedAt) {
}
