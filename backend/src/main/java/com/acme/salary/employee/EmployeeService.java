package com.acme.salary.employee;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import com.acme.salary.insights.InsightsService;
import com.acme.salary.reference.Country;
import com.acme.salary.reference.CountryRepository;
import com.acme.salary.web.ApiException;
import com.acme.salary.web.InvalidRequestException;
import com.acme.salary.web.NotFoundException;
import com.acme.salary.web.PageResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

	static final String NEW_HIRE_REASON = "New hire";

	private final EmployeeRepository employees;

	private final CountryRepository countries;

	private final InsightsService insights;

	private final Clock clock;

	EmployeeService(EmployeeRepository employees, CountryRepository countries, InsightsService insights,
			Clock clock) {
		this.employees = employees;
		this.countries = countries;
		this.insights = insights;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public PageResponse<EmployeeSummary> search(EmployeeFilter filter, EmployeeSort sort, int page, int size) {
		return PageResponse.of(employees.search(filter, sort, page, size), page, size, employees.count(filter));
	}

	@Transactional(readOnly = true)
	public EmployeeDetails get(long id) {
		Employee employee = employees.findById(id).orElseThrow(() -> notFound(id));
		Country country = countries.findByCode(employee.countryCode()).orElseThrow();
		return new EmployeeDetails(employee.id(), employee.fullName(), employee.email(), employee.jobTitle(),
				employee.department(), country.code(), country.name(), country.currencyCode(),
				employee.annualSalary(), country.toUsd(employee.annualSalary()), employee.hireDate(),
				employee.createdAt(), employee.updatedAt(), employees.findSalaryHistory(id),
				insights.peerComparison(country, employee.jobTitle(), employee.annualSalary()));
	}

	/** Creates the employee and the first entry of their salary history, effective from the hire date. */
	public EmployeeDetails create(EmployeeRequest request) {
		EmployeeRequest employee = request.normalized();
		Country country = requireCountry(employee.countryCode());
		requireEmailAvailable(employee.email(), null);

		Instant now = clock.instant();
		long id = employees.insert(employee, now);
		employees.insertSalaryChange(id, employee.annualSalary(), country.currencyCode(), employee.hireDate(),
				NEW_HIRE_REASON, now);
		return get(id);
	}

	/**
	 * Updates the employee. A new salary, or a move to a country with another currency, is appended to the
	 * salary history so the previous figure is never lost.
	 */
	public EmployeeDetails update(long id, EmployeeRequest request) {
		Employee existing = employees.findById(id).orElseThrow(() -> notFound(id));
		EmployeeRequest employee = request.normalized();
		Country country = requireCountry(employee.countryCode());
		requireEmailAvailable(employee.email(), id);

		Instant now = clock.instant();
		boolean salaryChanged = existing.annualSalary() != employee.annualSalary()
				|| !existing.countryCode().equals(employee.countryCode());
		if (salaryChanged) {
			LocalDate effectiveDate = employee.salaryEffectiveDate() != null ? employee.salaryEffectiveDate()
					: LocalDate.now(clock);
			if (effectiveDate.isBefore(employee.hireDate())) {
				throw new InvalidRequestException("salaryEffectiveDate", "must not be before the hire date");
			}
			employees.insertSalaryChange(id, employee.annualSalary(), country.currencyCode(), effectiveDate,
					employee.salaryChangeReason(), now);
		}
		employees.update(id, employee, now);
		return get(id);
	}

	public void delete(long id) {
		if (!employees.delete(id)) {
			throw notFound(id);
		}
	}

	@Transactional(readOnly = true)
	public void exportCsv(EmployeeFilter filter, EmployeeSort sort, Writer out) {
		try {
			out.write(EmployeeCsv.header());
			employees.forEachMatching(filter, sort, employee -> {
				try {
					out.write(EmployeeCsv.row(employee));
				}
				catch (IOException ex) {
					throw new UncheckedIOException(ex);
				}
			});
			out.flush();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private Country requireCountry(String code) {
		return countries.findByCode(code)
			.orElseThrow(() -> new InvalidRequestException("countryCode", "is not a country ACME employs in"));
	}

	private void requireEmailAvailable(String email, Long exceptEmployeeId) {
		if (employees.isEmailTaken(email, exceptEmployeeId)) {
			throw new DuplicateEmailException();
		}
	}

	private static NotFoundException notFound(long id) {
		return new NotFoundException("Employee not found", "No employee with id " + id);
	}

	static class DuplicateEmailException extends ApiException {

		DuplicateEmailException() {
			super(HttpStatus.CONFLICT, "Email already in use", "Another employee already uses this email",
					Map.of("email", "is already used by another employee"));
		}

	}

}
