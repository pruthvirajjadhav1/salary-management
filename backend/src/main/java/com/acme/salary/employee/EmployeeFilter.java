package com.acme.salary.employee;

import java.util.Locale;

/** Directory filters. Blank values mean "no filter". {@code query} matches name, email or employee ID. */
record EmployeeFilter(String query, String countryCode, String department, String jobTitle) {

	static EmployeeFilter of(String query, String countryCode, String department, String jobTitle) {
		String country = clean(countryCode);
		return new EmployeeFilter(clean(query), country == null ? null : country.toUpperCase(Locale.ROOT),
				clean(department), clean(jobTitle));
	}

	private static String clean(String value) {
		return (value == null || value.isBlank()) ? null : value.strip();
	}

}
