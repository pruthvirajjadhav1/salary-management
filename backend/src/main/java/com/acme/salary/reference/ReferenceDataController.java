package com.acme.salary.reference;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Values the UI needs for filters and form suggestions. Departments and job titles are derived from
 * existing employees so that suggestions steer HR towards consistent spelling.
 */
@RestController
@RequestMapping("/api/reference")
class ReferenceDataController {

	private final CountryRepository countries;

	private final JdbcClient jdbc;

	ReferenceDataController(CountryRepository countries, JdbcClient jdbc) {
		this.countries = countries;
		this.jdbc = jdbc;
	}

	@GetMapping
	ReferenceData referenceData() {
		return new ReferenceData(countries.findAll(), distinct("department"), distinct("job_title"));
	}

	private List<String> distinct(String column) {
		// column is one of two literals above, never user input
		return jdbc.sql("SELECT DISTINCT " + column + " FROM employee ORDER BY " + column + " COLLATE NOCASE")
			.query(String.class)
			.list();
	}

	record ReferenceData(List<Country> countries, List<String> departments, List<String> jobTitles) {
	}

}
