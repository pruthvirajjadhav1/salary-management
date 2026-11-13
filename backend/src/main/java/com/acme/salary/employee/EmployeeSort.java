package com.acme.salary.employee;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import com.acme.salary.web.InvalidRequestException;

/**
 * A whitelisted sort, parsed from {@code field[,asc|desc]}. Only these SQL fragments can ever reach
 * an {@code ORDER BY}, so the sort parameter cannot be used for SQL injection.
 */
record EmployeeSort(Field field, boolean descending) {

	static final EmployeeSort DEFAULT = new EmployeeSort(Field.NAME, false);

	enum Field {

		NAME("name", "e.full_name COLLATE NOCASE"),
		JOB_TITLE("jobTitle", "e.job_title COLLATE NOCASE"),
		DEPARTMENT("department", "e.department COLLATE NOCASE"),
		COUNTRY("country", "c.name"),
		// Salaries are in different currencies, so compare them in USD.
		SALARY("salary", "e.annual_salary / c.units_per_usd"),
		HIRE_DATE("hireDate", "e.hire_date");

		private final String apiName;

		private final String sql;

		Field(String apiName, String sql) {
			this.apiName = apiName;
			this.sql = sql;
		}

	}

	static EmployeeSort parse(String value) {
		if (value == null || value.isBlank()) {
			return DEFAULT;
		}
		String[] parts = value.split(",", -1);
		Field field = Arrays.stream(Field.values())
			.filter(candidate -> candidate.apiName.equals(parts[0].strip()))
			.findFirst()
			.orElseThrow(EmployeeSort::invalid);
		if (parts.length == 1) {
			return new EmployeeSort(field, false);
		}
		return switch (parts.length == 2 ? parts[1].strip().toLowerCase(Locale.ROOT) : "") {
			case "asc" -> new EmployeeSort(field, false);
			case "desc" -> new EmployeeSort(field, true);
			default -> throw invalid();
		};
	}

	String orderByClause() {
		return " ORDER BY " + field.sql + (descending ? " DESC" : " ASC") + ", e.id ASC";
	}

	private static InvalidRequestException invalid() {
		String fields = Arrays.stream(Field.values()).map(f -> f.apiName).collect(Collectors.joining(", "));
		return new InvalidRequestException("sort", "must be one of " + fields + ", optionally followed by ,asc or ,desc");
	}

}
