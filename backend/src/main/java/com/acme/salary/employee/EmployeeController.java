package com.acme.salary.employee;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.acme.salary.web.PageResponse;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
class EmployeeController {

	private final EmployeeService employees;

	private final Clock clock;

	EmployeeController(EmployeeService employees, Clock clock) {
		this.employees = employees;
		this.clock = clock;
	}

	@GetMapping
	PageResponse<EmployeeSummary> search(@RequestParam(required = false) String q,
			@RequestParam(required = false) String country, @RequestParam(required = false) String department,
			@RequestParam(required = false) String jobTitle,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "25") @Min(1) @Max(100) int size,
			@RequestParam(defaultValue = "name,asc") String sort) {
		return employees.search(EmployeeFilter.of(q, country, department, jobTitle), EmployeeSort.parse(sort), page,
				size);
	}

	/** The filtered directory as CSV, in the same order as the screen. */
	@GetMapping("/export")
	void export(@RequestParam(required = false) String q, @RequestParam(required = false) String country,
			@RequestParam(required = false) String department, @RequestParam(required = false) String jobTitle,
			@RequestParam(defaultValue = "name,asc") String sort, HttpServletResponse response) throws IOException {
		EmployeeFilter filter = EmployeeFilter.of(q, country, department, jobTitle);
		EmployeeSort order = EmployeeSort.parse(sort);
		response.setContentType("text/csv");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
			.filename("acme-employees-" + LocalDate.now(clock) + ".csv")
			.build()
			.toString());
		employees.exportCsv(filter, order, response.getWriter());
	}

	@GetMapping("/{id}")
	EmployeeDetails get(@PathVariable long id) {
		return employees.get(id);
	}

	@PostMapping
	ResponseEntity<EmployeeDetails> create(@Valid @RequestBody EmployeeRequest request) {
		EmployeeDetails created = employees.create(request);
		return ResponseEntity.created(URI.create("/api/employees/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	EmployeeDetails update(@PathVariable long id, @Valid @RequestBody EmployeeRequest request) {
		return employees.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable long id) {
		employees.delete(id);
	}

}
