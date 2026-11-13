package com.acme.salary.employee;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RFC 4180 CSV for the employee directory, safe to open in Excel.
 * <p>
 * Text that starts with {@code = + - @} (or a tab or carriage return) is prefixed with an apostrophe, so a
 * value typed into the app can never run as a spreadsheet formula (CSV injection).
 */
final class EmployeeCsv {

	private static final List<String> HEADER = List.of("Employee ID", "Full name", "Email", "Job title",
			"Department", "Country", "Currency", "Annual salary", "Annual salary (USD, reference rate)", "Hire date");

	private EmployeeCsv() {
	}

	static String header() {
		return line(HEADER.stream());
	}

	static String row(EmployeeSummary employee) {
		return line(Stream.of(String.valueOf(employee.id()), text(employee.fullName()), text(employee.email()),
				text(employee.jobTitle()), text(employee.department()), text(employee.countryName()),
				employee.currencyCode(), String.valueOf(employee.annualSalary()),
				String.valueOf(employee.annualSalaryUsd()), employee.hireDate().toString()));
	}

	/** Neutralises spreadsheet formulas in free text. Only applied to user-entered text, never to numbers. */
	static String text(String value) {
		if (!value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0) {
			return "'" + value;
		}
		return value;
	}

	private static String line(Stream<String> cells) {
		return cells.map(EmployeeCsv::quoteIfNeeded).collect(Collectors.joining(",", "", "\r\n"));
	}

	private static String quoteIfNeeded(String cell) {
		if (cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")) {
			return "\"" + cell.replace("\"", "\"\"") + "\"";
		}
		return cell;
	}

}
