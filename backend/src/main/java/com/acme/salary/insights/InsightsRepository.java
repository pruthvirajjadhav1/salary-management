package com.acme.salary.insights;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Read model over the employee table. Fetches only the four columns statistics need; grouping and
 * percentile maths happen in {@link InsightsService} (see docs/03-architecture.md for why).
 */
@Repository
class InsightsRepository {

	private static final String SELECT_POINTS = """
			SELECT country_code, department, job_title, annual_salary
			FROM employee
			""";

	private final JdbcClient jdbc;

	InsightsRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	List<SalaryPoint> findAll() {
		return jdbc.sql(SELECT_POINTS).query(SalaryPoint.class).list();
	}

	List<SalaryPoint> findByCountry(String countryCode) {
		return jdbc.sql(SELECT_POINTS + " WHERE country_code = :country")
			.param("country", countryCode)
			.query(SalaryPoint.class)
			.list();
	}

	List<Long> findSalaries(String countryCode, String jobTitle) {
		return jdbc.sql("SELECT annual_salary FROM employee WHERE country_code = :country AND job_title = :jobTitle")
			.param("country", countryCode)
			.param("jobTitle", jobTitle)
			.query(Long.class)
			.list();
	}

	record SalaryPoint(String countryCode, String department, String jobTitle, long annualSalary) {
	}

}
