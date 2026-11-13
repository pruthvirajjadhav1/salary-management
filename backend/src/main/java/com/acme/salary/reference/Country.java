package com.acme.salary.reference;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A country ACME employs people in. Salaries are paid in its currency; {@code unitsPerUsd} is a fixed
 * reference rate used only to compare and total pay across countries.
 */
public record Country(String code, String name, String currencyCode, BigDecimal unitsPerUsd, LocalDate rateAsOf) {

	public long toUsd(long amountInLocalCurrency) {
		return UsdConverter.toUsd(amountInLocalCurrency, unitsPerUsd);
	}

}
