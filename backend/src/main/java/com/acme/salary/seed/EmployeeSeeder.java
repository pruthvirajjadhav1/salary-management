package com.acme.salary.seed;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.acme.salary.reference.Country;
import com.acme.salary.reference.CountryRepository;
import com.acme.salary.seed.EmployeeGenerator.GeneratedEmployee;
import com.acme.salary.seed.EmployeeGenerator.SalaryEvent;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replaces all employees with a generated workforce in a single transaction, using JDBC batch inserts.
 * Takes well under a second for 10,000 employees and their salary histories on SQLite.
 */
@Component
public class EmployeeSeeder {

	private static final String INSERT_EMPLOYEE = """
			INSERT INTO employee (id, full_name, email, job_title, department, country_code, annual_salary,
			                      hire_date, created_at, updated_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""";

	private static final String INSERT_SALARY_CHANGE = """
			INSERT INTO salary_change (employee_id, annual_salary, currency_code, effective_date, reason, recorded_at)
			VALUES (?, ?, ?, ?, ?, ?)
			""";

	private static final int BATCH_SIZE = 1_000;

	private final JdbcTemplate jdbc;

	private final CountryRepository countries;

	private final Clock clock;

	EmployeeSeeder(JdbcTemplate jdbc, CountryRepository countries, Clock clock) {
		this.jdbc = jdbc;
		this.countries = countries;
		this.clock = clock;
	}

	public record Result(int employees, int salaryChanges) {
	}

	@Transactional(readOnly = true)
	public boolean hasNoEmployees() {
		Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class);
		return count == null || count == 0;
	}

	@Transactional
	public Result reseed(int count, long randomSeed) {
		List<Country> referenceCountries = countries.findAll();
		Map<String, Country> byCode = referenceCountries.stream()
			.collect(Collectors.toMap(Country::code, Function.identity()));
		List<GeneratedEmployee> employees = new EmployeeGenerator(referenceCountries).generate(count, randomSeed,
				LocalDate.now(clock));

		jdbc.update("DELETE FROM salary_change");
		jdbc.update("DELETE FROM employee");

		String now = Instant.now(clock).toString();
		List<Object[]> employeeRows = new ArrayList<>(employees.size());
		List<Object[]> historyRows = new ArrayList<>();
		for (int i = 0; i < employees.size(); i++) {
			GeneratedEmployee employee = employees.get(i);
			long id = i + 1L;
			employeeRows.add(new Object[] { id, employee.fullName(), employee.email(), employee.jobTitle(),
					employee.department(), employee.countryCode(), employee.annualSalary(),
					employee.hireDate().toString(), now, now });
			String currency = byCode.get(employee.countryCode()).currencyCode();
			for (SalaryEvent event : employee.history()) {
				historyRows.add(new Object[] { id, event.annualSalary(), currency, event.effectiveDate().toString(),
						event.reason(), now });
			}
		}
		insertInBatches(INSERT_EMPLOYEE, employeeRows);
		insertInBatches(INSERT_SALARY_CHANGE, historyRows);
		return new Result(employeeRows.size(), historyRows.size());
	}

	private void insertInBatches(String sql, List<Object[]> rows) {
		for (int from = 0; from < rows.size(); from += BATCH_SIZE) {
			jdbc.batchUpdate(sql, rows.subList(from, Math.min(from + BATCH_SIZE, rows.size())));
		}
	}

}
