package com.acme.salary.insights;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PayPositionTest {

	@Test
	void compaRatioIsSalaryDividedByPeerMedianToTwoDecimals() {
		PayPosition position = PayPosition.of(110_000, 100_000);

		assertThat(position.compaRatio()).isEqualByComparingTo(new BigDecimal("1.10"));
	}

	@Test
	void compaRatioRoundsHalfUp() {
		// 2/3 = 0.6666... -> 0.67
		assertThat(PayPosition.of(2, 3).compaRatio()).isEqualByComparingTo(new BigDecimal("0.67"));
	}

	@ParameterizedTest(name = "salary {0} against median 100,000 is {1}")
	@CsvSource({
		"85000,  BELOW",
		"89999,  BELOW",
		"90000,  WITHIN",
		"100000, WITHIN",
		"110000, WITHIN",
		"110001, ABOVE",
		"140000, ABOVE",
	})
	void bandsAreTenPercentEitherSideOfThePeerMedian(long salary, PayPosition.Band expected) {
		assertThat(PayPosition.of(salary, 100_000).band()).isEqualTo(expected);
	}

}
