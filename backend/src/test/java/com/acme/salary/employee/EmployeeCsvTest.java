package com.acme.salary.employee;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeCsvTest {

	@Test
	void quotesCellsContainingCommasAndDoublesEmbeddedQuotes() {
		EmployeeSummary employee = employee("Rao, \"Asha\"");

		assertThat(EmployeeCsv.row(employee)).startsWith("7,\"Rao, \"\"Asha\"\"\",");
	}

	@Test
	void neutralisesTextThatExcelWouldRunAsAFormula() {
		assertThat(EmployeeCsv.text("=HYPERLINK(\"http://evil\")")).isEqualTo("'=HYPERLINK(\"http://evil\")");
		assertThat(EmployeeCsv.text("+1-555")).isEqualTo("'+1-555");
		assertThat(EmployeeCsv.text("@SUM(A1)")).isEqualTo("'@SUM(A1)");
		assertThat(EmployeeCsv.text("Asha Rao")).isEqualTo("Asha Rao");
	}

	@Test
	void rowsEndWithCrLfAsRfc4180Requires() {
		assertThat(EmployeeCsv.header()).endsWith("Hire date\r\n");
		assertThat(EmployeeCsv.row(employee("Asha Rao"))).endsWith(",2022-04-01\r\n");
	}

	private static EmployeeSummary employee(String fullName) {
		return new EmployeeSummary(7, fullName, "asha@acme.example", "Software Engineer", "Engineering", "IN",
				"India", "INR", 2_400_000, 28_571, LocalDate.of(2022, 4, 1));
	}

}
