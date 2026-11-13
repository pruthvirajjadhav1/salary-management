package com.acme.salary;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
public class SalaryManagementApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(SalaryManagementApplication.class);
		application.addListeners(createDataDirectory());
		application.run(args);
	}

	/** SQLite creates the database file but not its parent directory, so make sure it exists first. */
	private static ApplicationListener<ApplicationEnvironmentPreparedEvent> createDataDirectory() {
		return event -> {
			String dataDir = event.getEnvironment().getProperty("app.data-dir", "./data");
			try {
				Files.createDirectories(Path.of(dataDir));
			}
			catch (IOException ex) {
				throw new UncheckedIOException("Cannot create data directory " + dataDir, ex);
			}
		};
	}

}
