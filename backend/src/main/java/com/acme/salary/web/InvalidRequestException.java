package com.acme.salary.web;

import java.util.Map;

import org.springframework.http.HttpStatus;

/** A request that is well-formed but breaks a business rule for one specific field. */
public class InvalidRequestException extends ApiException {

	public InvalidRequestException(String field, String message) {
		super(HttpStatus.BAD_REQUEST, "Validation failed", field + " " + message, Map.of(field, message));
	}

}
