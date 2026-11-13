package com.acme.salary.web;

import java.util.List;

/** One page of results. {@code page} is zero-based. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

	public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalItems) {
		int totalPages = (int) Math.ceilDiv(totalItems, (long) size);
		return new PageResponse<>(items, page, size, totalItems, totalPages);
	}

}
