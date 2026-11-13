package com.acme.salary.web;

import com.acme.salary.ApiTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class SpaForwardingTest {

	@Autowired
	MockMvcTester mvc;

	@Test
	void deepLinksIntoTheAppLoadIndexHtml() {
		assertThat(mvc.get().uri("/people?country=IN&employee=42")).hasForwardedUrl("/index.html");
		assertThat(mvc.get().uri("/overview/IN")).hasForwardedUrl("/index.html");
	}

	@Test
	void apiRoutesAreNeverForwarded() {
		assertThat(mvc.get().uri("/api/insights/overview")).hasStatusOk().hasForwardedUrl(null);
	}

}
