package com.acme.salary.insights;

import java.util.List;

import com.acme.salary.insights.InsightsService.CountryPay;
import com.acme.salary.insights.InsightsService.JobTitleBreakdown;
import com.acme.salary.insights.InsightsService.OrgOverview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
class InsightsController {

	private final InsightsService insights;

	InsightsController(InsightsService insights) {
		this.insights = insights;
	}

	@GetMapping("/overview")
	OrgOverview overview() {
		return insights.overview();
	}

	@GetMapping("/countries")
	List<CountryPay> countries() {
		return insights.byCountry();
	}

	@GetMapping("/countries/{countryCode}/job-titles")
	JobTitleBreakdown jobTitles(@PathVariable String countryCode) {
		return insights.jobTitlesIn(countryCode);
	}

}
