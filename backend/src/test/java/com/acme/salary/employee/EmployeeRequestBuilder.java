package com.acme.salary.employee;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/** Readable test data: {@code anEmployee().in("IN").titled("Software Engineer").earning(1_800_000).build()}. */
public final class EmployeeRequestBuilder {

	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	private String fullName = "Asha Rao";

	private String email = "employee" + SEQUENCE.incrementAndGet() + "@acme.example";

	private String jobTitle = "Software Engineer";

	private String department = "Engineering";

	private String countryCode = "US";

	private long annualSalary = 100_000;

	private LocalDate hireDate = LocalDate.of(2022, 4, 1);

	private EmployeeRequestBuilder() {
	}

	public static EmployeeRequestBuilder anEmployee() {
		return new EmployeeRequestBuilder();
	}

	public EmployeeRequestBuilder named(String fullName) {
		this.fullName = fullName;
		return this;
	}

	public EmployeeRequestBuilder withEmail(String email) {
		this.email = email;
		return this;
	}

	public EmployeeRequestBuilder titled(String jobTitle) {
		this.jobTitle = jobTitle;
		return this;
	}

	public EmployeeRequestBuilder inDepartment(String department) {
		this.department = department;
		return this;
	}

	public EmployeeRequestBuilder in(String countryCode) {
		this.countryCode = countryCode;
		return this;
	}

	public EmployeeRequestBuilder earning(long annualSalary) {
		this.annualSalary = annualSalary;
		return this;
	}

	public EmployeeRequestBuilder hiredOn(LocalDate hireDate) {
		this.hireDate = hireDate;
		return this;
	}

	public EmployeeRequest build() {
		return new EmployeeRequest(fullName, email, jobTitle, department, countryCode, annualSalary, hireDate, null,
				null);
	}

}
