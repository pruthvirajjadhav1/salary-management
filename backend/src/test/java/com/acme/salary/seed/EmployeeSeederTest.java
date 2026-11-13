package com.acme.salary.seed;

import com.acme.salary.ApiTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class EmployeeSeederTest {

	@Autowired
	EmployeeSeeder seeder;

	@Autowired
	JdbcClient jdbc;

	@Test
	void seedsTenThousandEmployeesWithTheirSalaryHistory() {
		EmployeeSeeder.Result result = seeder.reseed(10_000, 20261113);

		assertThat(result.employees()).isEqualTo(10_000);
		assertThat(count("employee")).isEqualTo(10_000);
		assertThat(count("salary_change")).isEqualTo(result.salaryChanges()).isGreaterThan(10_000);
		assertThat(seeder.hasNoEmployees()).isFalse();
	}

	@Test
	void reseedingReplacesRatherThanAppends() {
		seeder.reseed(50, 1);
		seeder.reseed(50, 2);

		assertThat(count("employee")).isEqualTo(50);
	}

	@Test
	void salaryHistoryUsesTheEmployeesCountryCurrency() {
		seeder.reseed(200, 7);

		long mismatched = jdbc.sql("""
				SELECT COUNT(*) FROM salary_change s
				JOIN employee e ON e.id = s.employee_id
				JOIN country c ON c.code = e.country_code
				WHERE s.currency_code <> c.currency_code
				""").query(Long.class).single();
		assertThat(mismatched).isZero();
	}

	private long count(String table) {
		return jdbc.sql("SELECT COUNT(*) FROM " + table).query(Long.class).single();
	}

}
