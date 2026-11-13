package com.acme.salary.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Where a salary sits relative to its peer group, expressed as a compa-ratio
 * (salary ÷ peer median). A ratio within ±10% of 1.00 is the conventional "in band" range.
 */
public record PayPosition(BigDecimal compaRatio, Band band) {

	public enum Band {

		BELOW, WITHIN, ABOVE

	}

	private static final BigDecimal LOWER_BOUND = new BigDecimal("0.90");

	private static final BigDecimal UPPER_BOUND = new BigDecimal("1.10");

	public static PayPosition of(long salary, long peerMedian) {
		if (peerMedian <= 0) {
			throw new IllegalArgumentException("Peer median must be positive");
		}
		BigDecimal exactRatio = BigDecimal.valueOf(salary).divide(BigDecimal.valueOf(peerMedian), 6, RoundingMode.HALF_UP);
		return new PayPosition(exactRatio.setScale(2, RoundingMode.HALF_UP), bandFor(exactRatio));
	}

	private static Band bandFor(BigDecimal ratio) {
		if (ratio.compareTo(LOWER_BOUND) < 0) {
			return Band.BELOW;
		}
		if (ratio.compareTo(UPPER_BOUND) > 0) {
			return Band.ABOVE;
		}
		return Band.WITHIN;
	}

}
