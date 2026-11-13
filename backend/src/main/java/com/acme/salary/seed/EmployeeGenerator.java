package com.acme.salary.seed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.acme.salary.reference.Country;
import com.acme.salary.seed.NamePools.NamePool;

/**
 * Generates a realistic, fully deterministic workforce: the same seed and date always produce the same
 * employees. Pure Java with no I/O, so it is unit-tested directly.
 * <p>
 * Salary model: a US benchmark per role × the country's pay level × the reference FX rate, a log-normal
 * spread between individuals and a small tenure premium, rounded to three significant figures the way
 * offers usually are. History is reconstructed backwards from today's salary through yearly April reviews.
 */
public final class EmployeeGenerator {

	record Role(String department, String jobTitle, int usBenchmarkUsd, int weight) {
	}

	record CountryProfile(String code, double payLevelVsUs, int weight) {
	}

	public record GeneratedEmployee(String fullName, String email, String jobTitle, String department,
			String countryCode, long annualSalary, LocalDate hireDate, List<SalaryEvent> history) {
	}

	public record SalaryEvent(LocalDate effectiveDate, long annualSalary, String reason) {
	}

	static final List<CountryProfile> COUNTRIES = List.of(new CountryProfile("US", 1.00, 30),
			new CountryProfile("IN", 0.28, 25), new CountryProfile("GB", 0.74, 12), new CountryProfile("DE", 0.78, 10),
			new CountryProfile("CA", 0.80, 8), new CountryProfile("BR", 0.32, 6), new CountryProfile("AU", 0.82, 5),
			new CountryProfile("SG", 0.80, 4));

	static final List<Role> ROLES = List.of(
			new Role("Engineering", "Software Engineer", 120_000, 14),
			new Role("Engineering", "Senior Software Engineer", 155_000, 10),
			new Role("Engineering", "Staff Engineer", 195_000, 3),
			new Role("Engineering", "Engineering Manager", 185_000, 3),
			new Role("Engineering", "QA Engineer", 95_000, 4),
			new Role("Engineering", "DevOps Engineer", 130_000, 4),
			new Role("Engineering", "Data Engineer", 130_000, 2),
			new Role("Product", "Product Manager", 140_000, 4),
			new Role("Product", "Senior Product Manager", 170_000, 2),
			new Role("Product", "Product Designer", 115_000, 2),
			new Role("Sales", "Sales Development Representative", 65_000, 6),
			new Role("Sales", "Account Executive", 110_000, 7),
			new Role("Sales", "Sales Manager", 150_000, 2),
			new Role("Sales", "Solutions Engineer", 135_000, 2),
			new Role("Marketing", "Marketing Specialist", 75_000, 3),
			new Role("Marketing", "Marketing Manager", 120_000, 2),
			new Role("Marketing", "Content Strategist", 80_000, 2),
			new Role("Customer Success", "Support Specialist", 55_000, 6),
			new Role("Customer Success", "Customer Success Manager", 85_000, 5),
			new Role("Customer Success", "Support Team Lead", 80_000, 1),
			new Role("Finance", "Financial Analyst", 90_000, 3),
			new Role("Finance", "Accountant", 75_000, 2),
			new Role("People", "Recruiter", 80_000, 2),
			new Role("People", "HR Business Partner", 105_000, 2),
			new Role("Operations", "Operations Analyst", 80_000, 2),
			new Role("Operations", "Data Analyst", 95_000, 2),
			new Role("Operations", "IT Support Specialist", 65_000, 2),
			new Role("Legal", "Legal Counsel", 170_000, 1));

	static final String NEW_HIRE = "New hire";

	static final String ANNUAL_REVIEW = "Annual review";

	static final String PROMOTION = "Promotion";

	private static final String EMAIL_DOMAIN = "acme.example";

	private static final int MAX_TENURE_DAYS = 12 * 365;

	private final Map<String, BigDecimal> unitsPerUsd;

	public EmployeeGenerator(List<Country> countries) {
		this.unitsPerUsd = countries.stream().collect(Collectors.toMap(Country::code, Country::unitsPerUsd));
		COUNTRIES.forEach(profile -> {
			if (!unitsPerUsd.containsKey(profile.code())) {
				throw new IllegalArgumentException("No reference rate for " + profile.code());
			}
		});
	}

	public List<GeneratedEmployee> generate(int count, long seed, LocalDate today) {
		Random random = new Random(seed);
		Set<String> usedEmails = new HashSet<>();
		List<GeneratedEmployee> employees = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			employees.add(generateOne(random, today, usedEmails));
		}
		return Collections.unmodifiableList(employees);
	}

	private GeneratedEmployee generateOne(Random random, LocalDate today, Set<String> usedEmails) {
		CountryProfile country = pickWeighted(random, COUNTRIES, CountryProfile::weight);
		Role role = pickWeighted(random, ROLES, Role::weight);
		NamePool names = NamePools.forCountry(country.code());
		String firstName = names.firstNames().get(random.nextInt(names.firstNames().size()));
		String lastName = names.lastNames().get(random.nextInt(names.lastNames().size()));

		// Skewed towards recent hires, as in a growing company.
		long tenureDays = (long) (Math.pow(random.nextDouble(), 1.7) * MAX_TENURE_DAYS);
		LocalDate hireDate = today.minusDays(tenureDays);
		double tenureYears = tenureDays / 365.0;

		double individualSpread = Math.clamp(Math.exp(random.nextGaussian() * 0.12), 0.70, 1.45);
		double tenurePremium = 1 + 0.025 * Math.min(tenureYears, 8);
		double salary = role.usBenchmarkUsd() * country.payLevelVsUs() * unitsPerUsd.get(country.code()).doubleValue()
				* individualSpread * tenurePremium;
		long currentSalary = roundToThreeSignificantFigures(salary);

		return new GeneratedEmployee(firstName + " " + lastName, uniqueEmail(firstName, lastName, usedEmails),
				role.jobTitle(), role.department(), country.code(), currentSalary, hireDate,
				history(random, hireDate, today, currentSalary));
	}

	/** Walks back from today's salary through each April review since hire, undoing that year's raise. */
	private static List<SalaryEvent> history(Random random, LocalDate hireDate, LocalDate today, long currentSalary) {
		List<SalaryEvent> raises = new ArrayList<>();
		long salaryAfter = currentSalary;
		for (int year = today.getYear(); year > hireDate.getYear(); year--) {
			LocalDate review = LocalDate.of(year, 4, 1);
			boolean eligible = !review.isAfter(today) && ChronoUnit.MONTHS.between(hireDate, review) >= 6;
			double roll = random.nextDouble();
			if (!eligible || roll < 0.12) {
				continue; // not yet eligible, or no raise that year
			}
			boolean promoted = roll > 0.94;
			double raise = promoted ? 0.10 + random.nextDouble() * 0.08 : 0.02 + random.nextDouble() * 0.05;
			raises.add(new SalaryEvent(review, salaryAfter, promoted ? PROMOTION : ANNUAL_REVIEW));
			salaryAfter = roundToThreeSignificantFigures(salaryAfter / (1 + raise));
		}
		List<SalaryEvent> history = new ArrayList<>(raises.size() + 1);
		history.add(new SalaryEvent(hireDate, salaryAfter, NEW_HIRE));
		history.addAll(raises.reversed());
		return List.copyOf(history);
	}

	private static String uniqueEmail(String firstName, String lastName, Set<String> usedEmails) {
		String localPart = emailSafe(firstName) + "." + emailSafe(lastName);
		String email = localPart + "@" + EMAIL_DOMAIN;
		for (int suffix = 2; !usedEmails.add(email); suffix++) {
			email = localPart + suffix + "@" + EMAIL_DOMAIN;
		}
		return email;
	}

	/** "Schäfer" -> "schafer", "O'Brien" -> "obrien", "Wei Ling" -> "weiling". */
	private static String emailSafe(String name) {
		return Normalizer.normalize(name, Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "")
			.replaceAll("[^A-Za-z]", "")
			.toLowerCase(Locale.ROOT);
	}

	static long roundToThreeSignificantFigures(double value) {
		return new BigDecimal(value).round(new MathContext(3, RoundingMode.HALF_UP)).longValue();
	}

	private static <T> T pickWeighted(Random random, List<T> items, ToIntFunction<T> weight) {
		int total = items.stream().mapToInt(weight).sum();
		int ticket = random.nextInt(total);
		for (T item : items) {
			ticket -= weight.applyAsInt(item);
			if (ticket < 0) {
				return item;
			}
		}
		throw new IllegalStateException("unreachable");
	}

}
