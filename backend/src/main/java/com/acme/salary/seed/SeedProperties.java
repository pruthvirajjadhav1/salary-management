package com.acme.salary.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param onStartup seed when the application starts and the database has no employees
 * @param reset wipe all employees and seed again on startup (used by {@code scripts/seed.sh})
 * @param employeeCount how many employees to generate
 * @param randomSeed fixes the generated data, so every environment gets the same employees
 */
@ConfigurationProperties("app.seed")
public record SeedProperties(@DefaultValue("false") boolean onStartup, @DefaultValue("false") boolean reset,
		@DefaultValue("10000") int employeeCount, @DefaultValue("20261113") long randomSeed) {
}
