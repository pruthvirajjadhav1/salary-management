package com.acme.salary.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Seeds an empty database on startup, or reseeds unconditionally when {@code app.seed.reset=true}. */
@Component
class SeedOnStartup implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SeedOnStartup.class);

	private final EmployeeSeeder seeder;

	private final SeedProperties properties;

	SeedOnStartup(EmployeeSeeder seeder, SeedProperties properties) {
		this.seeder = seeder;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		boolean seed = properties.reset() || (properties.onStartup() && seeder.hasNoEmployees());
		if (!seed) {
			return;
		}
		long started = System.nanoTime();
		EmployeeSeeder.Result result = seeder.reseed(properties.employeeCount(), properties.randomSeed());
		log.info("Seeded {} employees and {} salary history entries in {} ms (seed {})", result.employees(),
				result.salaryChanges(), (System.nanoTime() - started) / 1_000_000, properties.randomSeed());
	}

}
