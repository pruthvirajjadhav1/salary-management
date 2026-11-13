package com.acme.salary.insights;

import com.acme.salary.ApiTest;
import com.acme.salary.employee.EmployeeService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static com.acme.salary.employee.EmployeeRequestBuilder.anEmployee;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class InsightsApiTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	EmployeeService employees;

	@Test
	void anEmptyOrganisationHasNoHeadcountAndNoMedian() {
		var body = assertThat(mvc.get().uri("/api/insights/overview")).bodyJson();

		body.extractingPath("$.headcount").isEqualTo(0);
		body.extractingPath("$.medianSalaryUsd").isNull();
	}

	@Test
	void theOverviewConvertsEveryCountryToUsdBeforeTotalling() {
		employees.create(anEmployee().in("US").inDepartment("Engineering").earning(120_000).build());
		employees.create(anEmployee().in("IN").inDepartment("Engineering").earning(4_200_000).build()); // $50,000
		employees.create(anEmployee().in("GB").inDepartment("Sales").earning(79_000).build()); // $100,000

		var body = assertThat(mvc.get().uri("/api/insights/overview")).bodyJson();

		body.extractingPath("$.headcount").isEqualTo(3);
		body.extractingPath("$.countryCount").isEqualTo(3);
		body.extractingPath("$.totalPayrollUsd").isEqualTo(270_000);
		body.extractingPath("$.medianSalaryUsd").isEqualTo(100_000);
		body.extractingPath("$.ratesAsOf").isEqualTo("2026-11-01");
		body.extractingPath("$.departments[*].department").asArray().containsExactly("Engineering", "Sales");
		body.extractingPath("$.departments[0].totalPayrollUsd").isEqualTo(170_000);
	}

	@Test
	void countryStatisticsStayInLocalCurrency() {
		employees.create(anEmployee().in("IN").earning(1_800_000).build());
		employees.create(anEmployee().in("IN").earning(3_000_000).build());
		employees.create(anEmployee().in("US").earning(150_000).build());

		var body = assertThat(mvc.get().uri("/api/insights/countries")).bodyJson();

		body.extractingPath("$[0].countryCode").isEqualTo("IN");
		body.extractingPath("$[0].currencyCode").isEqualTo("INR");
		body.extractingPath("$[0].salary.headcount").isEqualTo(2);
		body.extractingPath("$[0].salary.min").isEqualTo(1_800_000);
		body.extractingPath("$[0].salary.max").isEqualTo(3_000_000);
		body.extractingPath("$[0].salary.average").isEqualTo(2_400_000);
		body.extractingPath("$[0].medianSalaryUsd").isEqualTo(28_571);
		body.extractingPath("$[1].countryCode").isEqualTo("US");
	}

	@Test
	void answersTheAverageSalaryForAJobTitleInACountry() {
		employees.create(anEmployee().in("IN").titled("Software Engineer").earning(1_800_000).build());
		employees.create(anEmployee().in("IN").titled("Software Engineer").earning(2_400_000).build());
		employees.create(anEmployee().in("IN").titled("Software Engineer").earning(3_000_000).build());
		employees.create(anEmployee().in("IN").titled("Product Manager").inDepartment("Product").earning(4_000_000)
			.build());
		employees.create(anEmployee().in("US").titled("Software Engineer").earning(150_000).build());

		var body = assertThat(mvc.get().uri("/api/insights/countries/in/job-titles")).bodyJson();

		body.extractingPath("$.country.name").isEqualTo("India");
		body.extractingPath("$.overall.headcount").isEqualTo(4);
		body.extractingPath("$.jobTitles[?(@.jobTitle == 'Software Engineer')].salary.average")
			.asArray()
			.containsExactly(2_400_000);
		body.extractingPath("$.jobTitles[?(@.jobTitle == 'Software Engineer')].salary.headcount")
			.asArray()
			.containsExactly(3);
	}

	@Test
	void anUnknownCountryIsNotFound() {
		assertThat(mvc.get().uri("/api/insights/countries/XX/job-titles")).hasStatus(HttpStatus.NOT_FOUND);
	}

}
