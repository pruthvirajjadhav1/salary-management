package com.acme.salary.web;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * A failure the client can act on. Rendered as an RFC 9457 problem detail, with optional field errors.
 */
public abstract class ApiException extends RuntimeException {

	private final HttpStatus status;

	private final String title;

	private final Map<String, String> fieldErrors;

	protected ApiException(HttpStatus status, String title, String detail, Map<String, String> fieldErrors) {
		super(detail);
		this.status = status;
		this.title = title;
		this.fieldErrors = Map.copyOf(fieldErrors);
	}

	public HttpStatus status() {
		return status;
	}

	public String title() {
		return title;
	}

	public Map<String, String> fieldErrors() {
		return fieldErrors;
	}

}
