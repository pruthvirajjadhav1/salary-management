package com.acme.salary.employee;

import com.acme.salary.ApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static com.acme.salary.employee.EmployeeRequestBuilder.anEmployee;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class EmployeeSearchApiTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	EmployeeService employees;

	@BeforeEach
	void someEmployees() {
		employees.create(anEmployee().named("Maya Patel").withEmail("maya.patel@acme.example").in("IN")
			.inDepartment("Engineering").titled("Software Engineer").earning(4_200_000).build()); // ≈ $50,000
		employees.create(anEmployee().named("Liam O'Brien").withEmail("liam.obrien@acme.example").in("GB")
			.inDepartment("Sales").titled("Account Executive").earning(79_000).build()); // ≈ $100,000
		employees.create(anEmployee().named("Noah Fischer").withEmail("noah.fischer@acme.example").in("DE")
			.inDepartment("Engineering").titled("Staff Engineer").earning(115_000).build()); // ≈ $125,000
		employees.create(anEmployee().named("Emma Patel").withEmail("emma.p@acme.example").in("US")
			.inDepartment("Engineering").titled("Software Engineer").earning(80_000).build()); // $80,000
	}

	@Test
	void searchesNamesAndEmailsIgnoringCase() {
		var body = assertThat(mvc.get().uri("/api/employees?q=PATEL")).bodyJson();

		body.extractingPath("$.totalItems").isEqualTo(2);
		body.extractingPath("$.items[*].fullName").asArray().containsExactly("Emma Patel", "Maya Patel");
	}

	@Test
	void treatsWildcardCharactersInTheSearchBoxLiterally() {
		assertThat(mvc.get().uri("/api/employees?q=%25")).bodyJson().extractingPath("$.totalItems").isEqualTo(0);
		assertThat(mvc.get().uri("/api/employees?q=_")).bodyJson().extractingPath("$.totalItems").isEqualTo(0);
	}

	@Test
	void combinesCountryAndDepartmentFilters() {
		var body = assertThat(mvc.get().uri("/api/employees?country=in&department=Engineering")).bodyJson();

		body.extractingPath("$.items[*].fullName").asArray().containsExactly("Maya Patel");
	}

	@Test
	void sortsSalariesByTheirUsdEquivalentNotTheirFaceValue() {
		var body = assertThat(mvc.get().uri("/api/employees?sort=salary,desc")).bodyJson();

		// Face values would put Maya's 4,200,000 INR first.
		body.extractingPath("$.items[*].fullName")
			.asArray()
			.containsExactly("Noah Fischer", "Liam O'Brien", "Emma Patel", "Maya Patel");
	}

	@Test
	void paginatesWithTotals() {
		var body = assertThat(mvc.get().uri("/api/employees?size=3&page=1")).bodyJson();

		body.extractingPath("$.items.length()").isEqualTo(1);
		body.extractingPath("$.page").isEqualTo(1);
		body.extractingPath("$.totalItems").isEqualTo(4);
		body.extractingPath("$.totalPages").isEqualTo(2);
	}

	@Test
	void rejectsUnknownSortFieldsAndOversizedPages() {
		assertThat(mvc.get().uri("/api/employees?sort=password")).hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.extractingPath("$.errors.sort")
			.asString()
			.startsWith("must be one of name, jobTitle");
		assertThat(mvc.get().uri("/api/employees?size=500")).hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.extractingPath("$.errors.size")
			.isNotNull();
	}

	@Test
	void exportsTheFilteredDirectoryAsCsvInScreenOrder() {
		var response = mvc.get().uri("/api/employees/export?department=Engineering&sort=salary,desc").exchange();

		assertThat(response).hasStatusOk()
			.headers()
			.hasValue("Content-Disposition", "attachment; filename=\"acme-employees-2026-11-13.csv\"");
		assertThat(response).bodyText()
			.startsWith("Employee ID,Full name,Email,Job title,Department,Country,Currency,Annual salary,")
			.contains("Noah Fischer,noah.fischer@acme.example,Staff Engineer,Engineering,Germany,EUR,115000,125000,")
			.doesNotContain("Liam");
		assertThat(response).bodyText().satisfies(csv -> assertThat(csv.lines()).hasSize(4));
	}

}
