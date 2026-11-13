package com.acme.salary.employee;

import java.time.LocalDate;

import com.acme.salary.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static com.acme.salary.employee.EmployeeRequestBuilder.anEmployee;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class EmployeeApiTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	EmployeeService employees;

	@Nested
	class Creating {

		@Test
		void storesTheEmployeeAndStartsTheirSalaryHistoryAsANewHire() {
			var response = mvc.post()
				.uri("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "fullName": "  Priya Sharma ",
						  "email": "Priya.Sharma@ACME.example",
						  "jobTitle": "Software Engineer",
						  "department": "Engineering",
						  "countryCode": "in",
						  "annualSalary": 2400000,
						  "hireDate": "2023-06-01"
						}
						""")
				.exchange();

			assertThat(response).hasStatus(HttpStatus.CREATED).headers().containsHeader("Location");
			var body = assertThat(response).bodyJson();
			body.extractingPath("$.fullName").isEqualTo("Priya Sharma");
			body.extractingPath("$.email").isEqualTo("priya.sharma@acme.example");
			body.extractingPath("$.countryName").isEqualTo("India");
			body.extractingPath("$.currencyCode").isEqualTo("INR");
			body.extractingPath("$.annualSalaryUsd").isEqualTo(28_571);
			body.extractingPath("$.salaryHistory.length()").isEqualTo(1);
			body.extractingPath("$.salaryHistory[0].reason").isEqualTo("New hire");
			body.extractingPath("$.salaryHistory[0].effectiveDate").isEqualTo("2023-06-01");
		}

		@Test
		void reportsEveryInvalidFieldAtOnce() {
			var response = mvc.post()
				.uri("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "fullName": " ", "email": "not-an-email", "jobTitle": "Analyst", "department": "Finance",
						  "countryCode": "US", "annualSalary": -5, "hireDate": null }
						""");

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST)
				.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
			var errors = assertThat(response).bodyJson().extractingPath("$.errors");
			errors.asMap().containsOnlyKeys("fullName", "email", "annualSalary", "hireDate");
		}

		@Test
		void rejectsACountryAcmeDoesNotEmployIn() {
			var response = post(anEmployee().in("FR").build());

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST)
				.bodyJson()
				.extractingPath("$.errors.countryCode")
				.isEqualTo("is not a country ACME employs in");
		}

		@Test
		void rejectsAnEmailAlreadyInUseIgnoringCase() {
			employees.create(anEmployee().withEmail("sam.lee@acme.example").build());

			var response = post(anEmployee().withEmail("SAM.LEE@acme.example").build());

			assertThat(response).hasStatus(HttpStatus.CONFLICT)
				.bodyJson()
				.extractingPath("$.errors.email")
				.isEqualTo("is already used by another employee");
		}

	}

	@Nested
	class Updating {

		@Test
		void aNewSalaryIsAppendedToHistoryWithItsEffectiveDateAndReason() {
			long id = employees.create(anEmployee().earning(100_000).hiredOn(LocalDate.of(2022, 1, 10)).build()).id();

			var response = put(id, """
					{ "fullName": "Asha Rao", "email": "asha@acme.example", "jobTitle": "Software Engineer",
					  "department": "Engineering", "countryCode": "US", "annualSalary": 112000,
					  "hireDate": "2022-01-10", "salaryEffectiveDate": "2026-01-01",
					  "salaryChangeReason": "Annual review" }
					""");

			assertThat(response).hasStatusOk();
			var body = assertThat(response).bodyJson();
			body.extractingPath("$.annualSalary").isEqualTo(112_000);
			body.extractingPath("$.salaryHistory.length()").isEqualTo(2);
			body.extractingPath("$.salaryHistory[0].annualSalary").isEqualTo(112_000);
			body.extractingPath("$.salaryHistory[0].effectiveDate").isEqualTo("2026-01-01");
			body.extractingPath("$.salaryHistory[0].reason").isEqualTo("Annual review");
			body.extractingPath("$.salaryHistory[1].annualSalary").isEqualTo(100_000);
		}

		@Test
		void aSalaryChangeWithoutAnEffectiveDateTakesEffectToday() {
			long id = employees.create(anEmployee().earning(100_000).build()).id();

			var response = put(id, """
					{ "fullName": "Asha Rao", "email": "asha@acme.example", "jobTitle": "Software Engineer",
					  "department": "Engineering", "countryCode": "US", "annualSalary": 105000,
					  "hireDate": "2022-04-01" }
					""");

			assertThat(response).bodyJson().extractingPath("$.salaryHistory[0].effectiveDate").isEqualTo(ApiTest.TODAY.toString());
		}

		@Test
		void editingOtherDetailsLeavesSalaryHistoryUntouched() {
			long id = employees.create(anEmployee().earning(100_000).build()).id();

			var response = put(id, """
					{ "fullName": "Asha Rao-Menon", "email": "asha@acme.example", "jobTitle": "Senior Software Engineer",
					  "department": "Engineering", "countryCode": "US", "annualSalary": 100000,
					  "hireDate": "2022-04-01" }
					""");

			var body = assertThat(response).bodyJson();
			body.extractingPath("$.fullName").isEqualTo("Asha Rao-Menon");
			body.extractingPath("$.salaryHistory.length()").isEqualTo(1);
		}

		@Test
		void relocatingRecordsTheSalaryInTheNewCountrysCurrency() {
			long id = employees.create(anEmployee().in("US").earning(100_000).build()).id();

			var response = put(id, """
					{ "fullName": "Asha Rao", "email": "asha@acme.example", "jobTitle": "Software Engineer",
					  "department": "Engineering", "countryCode": "GB", "annualSalary": 100000,
					  "hireDate": "2022-04-01", "salaryChangeReason": "Relocation to London" }
					""");

			var body = assertThat(response).bodyJson();
			body.extractingPath("$.salaryHistory.length()").isEqualTo(2);
			body.extractingPath("$.salaryHistory[0].currencyCode").isEqualTo("GBP");
			body.extractingPath("$.salaryHistory[1].currencyCode").isEqualTo("USD");
		}

		@Test
		void rejectsASalaryChangeEffectiveBeforeTheHireDate() {
			long id = employees.create(anEmployee().hiredOn(LocalDate.of(2024, 3, 1)).build()).id();

			var response = put(id, """
					{ "fullName": "Asha Rao", "email": "asha@acme.example", "jobTitle": "Software Engineer",
					  "department": "Engineering", "countryCode": "US", "annualSalary": 150000,
					  "hireDate": "2024-03-01", "salaryEffectiveDate": "2024-02-29" }
					""");

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST)
				.bodyJson()
				.extractingPath("$.errors.salaryEffectiveDate")
				.isEqualTo("must not be before the hire date");
		}

	}

	@Nested
	class ReadingAndDeleting {

		@Test
		void showsHowTheEmployeeIsPaidAgainstPeersWithTheSameTitleAndCountry() {
			employees.create(anEmployee().in("IN").titled("Data Analyst").earning(1_000_000).build());
			employees.create(anEmployee().in("IN").titled("Data Analyst").earning(1_200_000).build());
			long id = employees.create(anEmployee().in("IN").titled("Data Analyst").earning(1_500_000).build()).id();
			employees.create(anEmployee().in("US").titled("Data Analyst").earning(90_000).build());

			var body = assertThat(mvc.get().uri("/api/employees/{id}", id)).bodyJson();

			body.extractingPath("$.peerComparison.peers.headcount").isEqualTo(3);
			body.extractingPath("$.peerComparison.peers.median").isEqualTo(1_200_000);
			body.extractingPath("$.peerComparison.position.compaRatio").isEqualTo(1.25);
			body.extractingPath("$.peerComparison.position.band").isEqualTo("ABOVE");
		}

		@Test
		void deletingRemovesTheEmployee() {
			long id = employees.create(anEmployee().build()).id();

			assertThat(mvc.delete().uri("/api/employees/{id}", id)).hasStatus(HttpStatus.NO_CONTENT);
			assertThat(mvc.get().uri("/api/employees/{id}", id)).hasStatus(HttpStatus.NOT_FOUND);
		}

		@Test
		void unknownEmployeesAreNotFound() {
			assertThat(mvc.get().uri("/api/employees/999999")).hasStatus(HttpStatus.NOT_FOUND)
				.bodyJson()
				.extractingPath("$.title")
				.isEqualTo("Employee not found");
			assertThat(mvc.delete().uri("/api/employees/999999")).hasStatus(HttpStatus.NOT_FOUND);
			assertThat(put(999_999, """
					{ "fullName": "Nobody", "email": "nobody@acme.example", "jobTitle": "Analyst",
					  "department": "Finance", "countryCode": "US", "annualSalary": 1, "hireDate": "2020-01-01" }
					""")).hasStatus(HttpStatus.NOT_FOUND);
		}

	}

	private MvcTestResult post(EmployeeRequest request) {
		return mvc.post()
			.uri("/api/employees")
			.contentType(MediaType.APPLICATION_JSON)
			.content(json(request))
			.exchange();
	}

	private MvcTestResult put(long id, String json) {
		return mvc.put()
			.uri("/api/employees/{id}", id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json)
			.exchange();
	}

	private static String json(EmployeeRequest r) {
		return """
				{ "fullName": "%s", "email": "%s", "jobTitle": "%s", "department": "%s", "countryCode": "%s",
				  "annualSalary": %d, "hireDate": "%s" }
				""".formatted(r.fullName(), r.email(), r.jobTitle(), r.department(), r.countryCode(),
				r.annualSalary(), r.hireDate());
	}

}
