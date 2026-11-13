package com.acme.salary.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * Descriptive statistics for a group of annual salaries in a single currency.
 * <p>
 * Percentiles use linear interpolation between closest ranks, the same method as Excel's
 * {@code PERCENTILE.INC} and {@code MEDIAN}, so figures reconcile with HR's spreadsheets.
 * Every value is rounded half-up to whole currency units.
 */
public record SalaryStatistics(int headcount, long min, long p25, long median, long average, long p75, long max,
		long total) {

	public static SalaryStatistics of(Collection<Long> salaries) {
		if (salaries.isEmpty()) {
			throw new IllegalArgumentException("Statistics need at least one salary");
		}
		long[] sorted = salaries.stream().mapToLong(Long::longValue).sorted().toArray();
		long total = 0;
		for (long salary : sorted) {
			total = Math.addExact(total, salary);
		}
		return new SalaryStatistics(sorted.length, sorted[0], percentile(sorted, 0.25), percentile(sorted, 0.5),
				divideRoundingHalfUp(total, sorted.length), percentile(sorted, 0.75), sorted[sorted.length - 1],
				total);
	}

	private static long percentile(long[] sorted, double fraction) {
		double rank = fraction * (sorted.length - 1);
		int lower = (int) Math.floor(rank);
		int upper = (int) Math.ceil(rank);
		double interpolated = sorted[lower] + (rank - lower) * (sorted[upper] - sorted[lower]);
		return Math.round(interpolated);
	}

	private static long divideRoundingHalfUp(long dividend, long divisor) {
		return BigDecimal.valueOf(dividend).divide(BigDecimal.valueOf(divisor), 0, RoundingMode.HALF_UP).longValueExact();
	}

}
