package com.acme.salary.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acme.salary.insights.SalaryStatistics;
import com.acme.salary.reference.Country;
import com.acme.salary.seed.EmployeeGenerator.GeneratedEmployee;
import com.acme.salary.seed.EmployeeGenerator.SalaryEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class EmployeeGeneratorTest {

	static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

	static final List<Country> COUNTRIES = List.of(country("US", "USD", "1.00"), country("GB", "GBP", "0.79"),
			country("DE", "EUR", "0.92"), country("IN", "INR", "84.00"), country("CA", "CAD", "1.38"),
			country("AU", "AUD", "1.53"), country("SG", "SGD", "1.34"), country("BR", "BRL", "5.70"));

	static List<GeneratedEmployee> workforce;

	@BeforeAll
	static void generateTenThousand() {
		workforce = new EmployeeGenerator(COUNTRIES).generate(10_000, 42, TODAY);
	}

	@Test
	void generatesExactlyTheRequestedHeadcount() {
		assertThat(workforce).hasSize(10_000);
	}

	@Test
	void theSameSeedAlwaysProducesTheSameWorkforce() {
		List<GeneratedEmployee> again = new EmployeeGenerator(COUNTRIES).generate(10_000, 42, TODAY);
		List<GeneratedEmployee> otherSeed = new EmployeeGenerator(COUNTRIES).generate(10_000, 43, TODAY);

		assertThat(again).isEqualTo(workforce);
		assertThat(otherSeed).isNotEqualTo(workforce);
	}

	@Test
	void everyEmailIsUnique() {
		assertThat(workforce).extracting(GeneratedEmployee::email).doesNotHaveDuplicates();
	}

	@Test
	void emailsAreAsciiEvenForAccentedNames() {
		assertThat(workforce).extracting(GeneratedEmployee::email)
			.allMatch(email -> email.matches("[a-z]+\\.[a-z]+\\d*@acme\\.example"));
	}

	@Test
	void headcountIsSpreadAcrossCountriesByTheirWeight() {
		Map<String, Long> byCountry = workforce.stream()
			.collect(Collectors.groupingBy(GeneratedEmployee::countryCode, Collectors.counting()));

		assertThat(byCountry).containsOnlyKeys("US", "IN", "GB", "DE", "CA", "BR", "AU", "SG");
		assertThat(byCountry.get("US") / 10_000.0).isCloseTo(0.30, within(0.02));
		assertThat(byCountry.get("IN") / 10_000.0).isCloseTo(0.25, within(0.02));
		assertThat(byCountry.get("SG") / 10_000.0).isCloseTo(0.04, within(0.01));
	}

	@Test
	void salariesAreRealisticInEachLocalCurrency() {
		assertThat(medianSalary("US", "Software Engineer")).isBetween(110_000L, 160_000L);
		assertThat(medianSalary("IN", "Software Engineer")).isBetween(2_500_000L, 4_000_000L);
		assertThat(medianSalary("GB", "Support Specialist")).isBetween(30_000L, 45_000L);
	}

	@Test
	void salariesAreRoundedLikeRealOffers() {
		assertThat(workforce).extracting(GeneratedEmployee::annualSalary)
			.allMatch(salary -> salary == EmployeeGenerator.roundToThreeSignificantFigures(salary));
	}

	@Test
	void hireDatesAreInThePastTwelveYears() {
		assertThat(workforce).extracting(GeneratedEmployee::hireDate)
			.allMatch(date -> !date.isAfter(TODAY) && date.isAfter(TODAY.minusYears(13)));
	}

	@Test
	void historyStartsAtHireAndEndsAtTodaysSalaryInDateOrder() {
		assertThat(workforce).allSatisfy(employee -> {
			List<SalaryEvent> history = employee.history();
			assertThat(history.getFirst().reason()).isEqualTo(EmployeeGenerator.NEW_HIRE);
			assertThat(history.getFirst().effectiveDate()).isEqualTo(employee.hireDate());
			assertThat(history.getLast().annualSalary()).isEqualTo(employee.annualSalary());
			assertThat(history).extracting(SalaryEvent::effectiveDate).isSorted();
			assertThat(history).extracting(SalaryEvent::annualSalary).isSorted();
		});
	}

	@Test
	void longServingEmployeesHaveSeveralRaises() {
		double averageHistoryLength = workforce.stream()
			.filter(employee -> employee.hireDate().isBefore(TODAY.minusYears(5)))
			.mapToInt(employee -> employee.history().size())
			.average()
			.orElseThrow();

		assertThat(averageHistoryLength).isGreaterThan(4);
	}

	private static long medianSalary(String country, String jobTitle) {
		return SalaryStatistics.of(workforce.stream()
			.filter(employee -> employee.countryCode().equals(country) && employee.jobTitle().equals(jobTitle))
			.map(GeneratedEmployee::annualSalary)
			.toList()).median();
	}

	private static Country country(String code, String currency, String unitsPerUsd) {
		return new Country(code, code, currency, new BigDecimal(unitsPerUsd), LocalDate.of(2026, 11, 1));
	}

}
