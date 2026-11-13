package com.acme.salary.reference;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Converts whole units of a local currency to whole US dollars at a reference rate. */
public final class UsdConverter {

	private UsdConverter() {
	}

	public static long toUsd(long amountInLocalCurrency, BigDecimal unitsPerUsd) {
		return BigDecimal.valueOf(amountInLocalCurrency)
			.divide(unitsPerUsd, 0, RoundingMode.HALF_UP)
			.longValueExact();
	}

}
