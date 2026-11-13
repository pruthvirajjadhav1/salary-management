package com.acme.salary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

/**
 * Full application against a throwaway SQLite database (schema built by Flyway, no seed data), with the
 * clock fixed at {@link ApiTest#TODAY}. Each test runs in a transaction that is rolled back, so tests never
 * see each other's data. All API tests share one Spring context, which starts once.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(ApiTest.FixedClock.class)
public @interface ApiTest {

	LocalDate TODAY = LocalDate.of(2026, 11, 13);

	@TestConfiguration(proxyBeanMethods = false)
	class FixedClock {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(Instant.parse(TODAY + "T09:30:00Z"), ZoneOffset.UTC);
		}

	}

}
