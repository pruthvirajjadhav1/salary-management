package com.acme.salary.employee;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.acme.salary.employee.EmployeeDetails.SalaryChange;
import com.acme.salary.reference.UsdConverter;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class EmployeeRepository {

	private static final String SELECT_SUMMARY = """
			SELECT e.id, e.full_name, e.email, e.job_title, e.department, e.country_code,
			       c.name AS country_name, c.currency_code, c.units_per_usd, e.annual_salary, e.hire_date
			FROM employee e
			JOIN country c ON c.code = e.country_code
			""";

	private static final String SELECT_EMPLOYEE = """
			SELECT id, full_name, email, job_title, department, country_code, annual_salary, hire_date,
			       created_at, updated_at
			FROM employee
			""";

	private final JdbcClient jdbc;

	EmployeeRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	List<EmployeeSummary> search(EmployeeFilter filter, EmployeeSort sort, int page, int size) {
		Where where = Where.of(filter);
		return jdbc.sql(SELECT_SUMMARY + where.sql() + sort.orderByClause() + " LIMIT :limit OFFSET :offset")
			.params(where.params())
			.param("limit", size)
			.param("offset", (long) page * size)
			.query(EmployeeRepository::mapSummary)
			.list();
	}

	long count(EmployeeFilter filter) {
		Where where = Where.of(filter);
		return jdbc.sql("SELECT COUNT(*) FROM employee e" + where.sql())
			.params(where.params())
			.query(Long.class)
			.single();
	}

	/** Streams every matching row without loading the whole result into memory (used for CSV export). */
	void forEachMatching(EmployeeFilter filter, EmployeeSort sort, Consumer<EmployeeSummary> action) {
		Where where = Where.of(filter);
		jdbc.sql(SELECT_SUMMARY + where.sql() + sort.orderByClause())
			.params(where.params())
			.query(rs -> {
				action.accept(mapSummary(rs, 0));
			});
	}

	Optional<Employee> findById(long id) {
		return jdbc.sql(SELECT_EMPLOYEE + " WHERE id = :id")
			.param("id", id)
			.query(EmployeeRepository::mapEmployee)
			.optional();
	}

	boolean isEmailTaken(String email, Long exceptEmployeeId) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM employee WHERE email = :email AND id IS NOT :exceptId)")
			.param("email", email)
			.param("exceptId", exceptEmployeeId)
			.query(Boolean.class)
			.single();
	}

	long insert(EmployeeRequest employee, Instant now) {
		KeyHolder keys = new GeneratedKeyHolder();
		jdbc.sql("""
				INSERT INTO employee (full_name, email, job_title, department, country_code, annual_salary,
				                      hire_date, created_at, updated_at)
				VALUES (:fullName, :email, :jobTitle, :department, :countryCode, :annualSalary,
				        :hireDate, :now, :now)
				""")
			.params(columnsOf(employee))
			.param("now", now.toString())
			.update(keys);
		return keys.getKeyAs(Number.class).longValue();
	}

	void update(long id, EmployeeRequest employee, Instant now) {
		jdbc.sql("""
				UPDATE employee
				SET full_name = :fullName, email = :email, job_title = :jobTitle, department = :department,
				    country_code = :countryCode, annual_salary = :annualSalary, hire_date = :hireDate,
				    updated_at = :now
				WHERE id = :id
				""")
			.params(columnsOf(employee))
			.param("now", now.toString())
			.param("id", id)
			.update();
	}

	boolean delete(long id) {
		return jdbc.sql("DELETE FROM employee WHERE id = :id").param("id", id).update() > 0;
	}

	void insertSalaryChange(long employeeId, long annualSalary, String currencyCode, LocalDate effectiveDate,
			String reason, Instant recordedAt) {
		jdbc.sql("""
				INSERT INTO salary_change (employee_id, annual_salary, currency_code, effective_date, reason,
				                           recorded_at)
				VALUES (:employeeId, :annualSalary, :currencyCode, :effectiveDate, :reason, :recordedAt)
				""")
			.param("employeeId", employeeId)
			.param("annualSalary", annualSalary)
			.param("currencyCode", currencyCode)
			.param("effectiveDate", effectiveDate.toString())
			.param("reason", reason)
			.param("recordedAt", recordedAt.toString())
			.update();
	}

	/** Newest first. */
	List<SalaryChange> findSalaryHistory(long employeeId) {
		return jdbc.sql("""
				SELECT id, annual_salary, currency_code, effective_date, reason, recorded_at
				FROM salary_change
				WHERE employee_id = :employeeId
				ORDER BY effective_date DESC, id DESC
				""")
			.param("employeeId", employeeId)
			.query((rs, rowNum) -> new SalaryChange(rs.getLong("id"), rs.getLong("annual_salary"),
					rs.getString("currency_code"), LocalDate.parse(rs.getString("effective_date")),
					rs.getString("reason"), Instant.parse(rs.getString("recorded_at"))))
			.list();
	}

	private static Map<String, Object> columnsOf(EmployeeRequest employee) {
		Map<String, Object> columns = new HashMap<>();
		columns.put("fullName", employee.fullName());
		columns.put("email", employee.email());
		columns.put("jobTitle", employee.jobTitle());
		columns.put("department", employee.department());
		columns.put("countryCode", employee.countryCode());
		columns.put("annualSalary", employee.annualSalary());
		columns.put("hireDate", employee.hireDate().toString());
		return columns;
	}

	private static EmployeeSummary mapSummary(ResultSet rs, int rowNum) throws SQLException {
		long salary = rs.getLong("annual_salary");
		return new EmployeeSummary(rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
				rs.getString("job_title"), rs.getString("department"), rs.getString("country_code"),
				rs.getString("country_name"), rs.getString("currency_code"), salary,
				UsdConverter.toUsd(salary, rs.getBigDecimal("units_per_usd")),
				LocalDate.parse(rs.getString("hire_date")));
	}

	private static Employee mapEmployee(ResultSet rs, int rowNum) throws SQLException {
		return new Employee(rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
				rs.getString("job_title"), rs.getString("department"), rs.getString("country_code"),
				rs.getLong("annual_salary"), LocalDate.parse(rs.getString("hire_date")),
				Instant.parse(rs.getString("created_at")), Instant.parse(rs.getString("updated_at")));
	}

	/** Builds a WHERE clause from only the filters that are set, so SQLite can use the matching index. */
	private record Where(String sql, Map<String, Object> params) {

		static Where of(EmployeeFilter filter) {
			List<String> clauses = new ArrayList<>();
			Map<String, Object> params = new HashMap<>();
			if (filter.query() != null) {
				String text = "(e.full_name LIKE :pattern ESCAPE '\\' OR e.email LIKE :pattern ESCAPE '\\'";
				params.put("pattern", "%" + escapeLike(filter.query()) + "%");
				if (filter.query().chars().allMatch(Character::isDigit) && filter.query().length() <= 18) {
					text += " OR e.id = :id";
					params.put("id", Long.parseLong(filter.query()));
				}
				clauses.add(text + ")");
			}
			if (filter.countryCode() != null) {
				clauses.add("e.country_code = :country");
				params.put("country", filter.countryCode());
			}
			if (filter.department() != null) {
				clauses.add("e.department = :department");
				params.put("department", filter.department());
			}
			if (filter.jobTitle() != null) {
				clauses.add("e.job_title = :jobTitle");
				params.put("jobTitle", filter.jobTitle());
			}
			return new Where(clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses), params);
		}

		/** Treat %, _ and \ in the search box as literal characters, not wildcards. */
		private static String escapeLike(String text) {
			return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		}

	}

}
