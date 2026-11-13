package com.acme.salary.insights;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SalaryStatisticsTest {

	@Test
	void aSingleSalaryIsEveryStatistic() {
		SalaryStatistics stats = SalaryStatistics.of(List.of(72_000L));

		assertThat(stats.headcount()).isEqualTo(1);
		assertThat(stats.min()).isEqualTo(72_000);
		assertThat(stats.p25()).isEqualTo(72_000);
		assertThat(stats.median()).isEqualTo(72_000);
		assertThat(stats.average()).isEqualTo(72_000);
		assertThat(stats.p75()).isEqualTo(72_000);
		assertThat(stats.max()).isEqualTo(72_000);
		assertThat(stats.total()).isEqualTo(72_000);
	}

	@Test
	void medianOfAnOddCountIsTheMiddleSalaryRegardlessOfInputOrder() {
		SalaryStatistics stats = SalaryStatistics.of(List.of(90_000L, 30_000L, 50_000L));

		assertThat(stats.median()).isEqualTo(50_000);
	}

	@Test
	void medianOfAnEvenCountIsTheMeanOfTheTwoMiddleSalaries() {
		SalaryStatistics stats = SalaryStatistics.of(List.of(40_000L, 50_000L, 60_000L, 90_000L));

		assertThat(stats.median()).isEqualTo(55_000);
	}

	@Test
	void quartilesInterpolateTheSameWayAsExcelPercentileInc() {
		// =PERCENTILE.INC({40000,50000,60000,90000}, 0.25) -> 47500 and 0.75 -> 67500
		SalaryStatistics stats = SalaryStatistics.of(List.of(60_000L, 90_000L, 40_000L, 50_000L));

		assertThat(stats.p25()).isEqualTo(47_500);
		assertThat(stats.p75()).isEqualTo(67_500);
	}

	@Test
	void reportsHeadcountMinMaxAndTotal() {
		SalaryStatistics stats = SalaryStatistics.of(List.of(55_000L, 120_000L, 38_000L, 61_000L));

		assertThat(stats.headcount()).isEqualTo(4);
		assertThat(stats.min()).isEqualTo(38_000);
		assertThat(stats.max()).isEqualTo(120_000);
		assertThat(stats.total()).isEqualTo(274_000);
	}

	@Test
	void averageIsRoundedHalfUpToWholeCurrencyUnits() {
		assertThat(SalaryStatistics.of(List.of(100L, 101L)).average()).isEqualTo(101);
		assertThat(SalaryStatistics.of(List.of(100L, 100L, 101L)).average()).isEqualTo(100);
	}

	@Test
	void interpolatedPercentilesAreRoundedHalfUpToWholeCurrencyUnits() {
		assertThat(SalaryStatistics.of(List.of(1L, 2L)).median()).isEqualTo(2);
	}

	@Test
	void totalsTenThousandLargeSalariesWithoutOverflow() {
		// 10,000 people on INR 5 crore each: 5 * 10^12, far beyond int range.
		List<Long> salaries = Collections.nCopies(10_000, 500_000_000L);

		SalaryStatistics stats = SalaryStatistics.of(salaries);

		assertThat(stats.total()).isEqualTo(5_000_000_000_000L);
		assertThat(stats.average()).isEqualTo(500_000_000L);
	}

	@Test
	void refusesToDescribeAnEmptyGroup() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> SalaryStatistics.of(List.of()))
			.withMessageContaining("at least one salary");
	}

}
