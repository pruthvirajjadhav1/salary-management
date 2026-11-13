package com.acme.salary.web;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

	public NotFoundException(String title, String detail) {
		super(HttpStatus.NOT_FOUND, title, detail, Map.of());
	}

}
