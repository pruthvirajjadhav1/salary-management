package com.acme.salary.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The Angular app is served from {@code /static}. Its client-side routes are forwarded to
 * {@code index.html} so deep links and browser reloads work; {@code /api/**} is never matched here.
 */
@Controller
class SpaForwardingController {

	@GetMapping({ "/overview", "/overview/{countryCode}", "/people" })
	String forwardToApp() {
		return "forward:/index.html";
	}

}
