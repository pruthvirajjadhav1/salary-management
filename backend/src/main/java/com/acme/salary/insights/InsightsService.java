package com.acme.salary.insights;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.acme.salary.insights.InsightsRepository.SalaryPoint;
import com.acme.salary.reference.Country;
import com.acme.salary.reference.CountryRepository;
import com.acme.salary.web.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Answers "how do we pay people?". Statistics within a country stay in its local currency;
 * anything that crosses countries is converted to USD at the reference rate first.
 */
@Service
@Transactional(readOnly = true)
public class InsightsService {

	private final InsightsRepository repository;

	private final CountryRepository countries;

	InsightsService(InsightsRepository repository, CountryRepository countries) {
		this.repository = repository;
		this.countries = countries;
	}

	public OrgOverview overview() {
		Map<String, Country> countriesByCode = countries.findAllByCode();
		LocalDate ratesAsOf = countriesByCode.values()
			.stream()
			.map(Country::rateAsOf)
			.max(Comparator.naturalOrder())
			.orElse(null);
		List<SalaryPoint> points = repository.findAll();
		if (points.isEmpty()) {
			return new OrgOverview(0, 0, 0, null, null, ratesAsOf, List.of());
		}

		Function<SalaryPoint, Long> toUsd = point -> countriesByCode.get(point.countryCode())
			.toUsd(point.annualSalary());
		SalaryStatistics orgUsd = SalaryStatistics.of(points.stream().map(toUsd).toList());
		int countryCount = (int) points.stream().map(SalaryPoint::countryCode).distinct().count();

		List<DepartmentPay> departments = points.stream()
			.collect(Collectors.groupingBy(SalaryPoint::department, Collectors.mapping(toUsd, Collectors.toList())))
			.entrySet()
			.stream()
			.map(entry -> {
				SalaryStatistics usd = SalaryStatistics.of(entry.getValue());
				return new DepartmentPay(entry.getKey(), usd.headcount(), usd.total(), usd.median());
			})
			.sorted(Comparator.comparingLong(DepartmentPay::totalPayrollUsd).reversed())
			.toList();

		return new OrgOverview(orgUsd.headcount(), countryCount, orgUsd.total(), orgUsd.median(), orgUsd.average(),
				ratesAsOf, departments);
	}

	public List<CountryPay> byCountry() {
		Map<String, Country> countriesByCode = countries.findAllByCode();
		return repository.findAll()
			.stream()
			.collect(Collectors.groupingBy(SalaryPoint::countryCode,
					Collectors.mapping(SalaryPoint::annualSalary, Collectors.toList())))
			.entrySet()
			.stream()
			.map(entry -> {
				Country country = countriesByCode.get(entry.getKey());
				SalaryStatistics local = SalaryStatistics.of(entry.getValue());
				return new CountryPay(country.code(), country.name(), country.currencyCode(), local,
						country.toUsd(local.median()), country.toUsd(local.total()));
			})
			.sorted(Comparator.comparingInt((CountryPay pay) -> pay.salary().headcount()).reversed())
			.toList();
	}

	public JobTitleBreakdown jobTitlesIn(String countryCode) {
		Country country = countries.findByCode(countryCode.toUpperCase())
			.orElseThrow(() -> new NotFoundException("Country not found", "No country with code " + countryCode));
		List<SalaryPoint> points = repository.findByCountry(country.code());
		if (points.isEmpty()) {
			return new JobTitleBreakdown(country, null, List.of());
		}

		SalaryStatistics overall = SalaryStatistics.of(points.stream().map(SalaryPoint::annualSalary).toList());
		List<JobTitlePay> jobTitles = points.stream()
			.collect(Collectors.groupingBy(point -> new JobKey(point.department(), point.jobTitle()),
					Collectors.mapping(SalaryPoint::annualSalary, Collectors.toList())))
			.entrySet()
			.stream()
			.map(entry -> new JobTitlePay(entry.getKey().jobTitle(), entry.getKey().department(),
					SalaryStatistics.of(entry.getValue())))
			.sorted(Comparator.comparingLong((JobTitlePay pay) -> pay.salary().median()).reversed())
			.toList();
		return new JobTitleBreakdown(country, overall, jobTitles);
	}

	/** Peers share the employee's job title and country, and include the employee. */
	public PeerComparison peerComparison(Country country, String jobTitle, long salary) {
		List<Long> peerSalaries = repository.findSalaries(country.code(), jobTitle);
		if (peerSalaries.isEmpty()) {
			return null;
		}
		SalaryStatistics peers = SalaryStatistics.of(peerSalaries);
		return new PeerComparison(jobTitle, country.code(), country.currencyCode(), peers,
				PayPosition.of(salary, peers.median()));
	}

	private record JobKey(String department, String jobTitle) {
	}

	public record OrgOverview(int headcount, int countryCount, long totalPayrollUsd, Long medianSalaryUsd,
			Long averageSalaryUsd, LocalDate ratesAsOf, List<DepartmentPay> departments) {
	}

	public record DepartmentPay(String department, int headcount, long totalPayrollUsd, long medianSalaryUsd) {
	}

	public record CountryPay(String countryCode, String countryName, String currencyCode, SalaryStatistics salary,
			long medianSalaryUsd, long totalPayrollUsd) {
	}

	public record JobTitleBreakdown(Country country, SalaryStatistics overall, List<JobTitlePay> jobTitles) {
	}

	public record JobTitlePay(String jobTitle, String department, SalaryStatistics salary) {
	}

	public record PeerComparison(String jobTitle, String countryCode, String currencyCode, SalaryStatistics peers,
			PayPosition position) {
	}

}
