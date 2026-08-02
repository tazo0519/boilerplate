package com.example.boilerplate.common;

import java.util.List;

public class ResponseBuilder {

	public static <T> Response<DataResponse<T>> build(List<T> items) {
		return new Response<>(new DataResponse<>(items));
	}

	public static <T> Response<DataResponse<T>> build(List<T> items, Integer page, Integer size, Long totalSize) {
		return new Response<>(new DataResponse<>(items, page, size, totalSize));
	}

	public static Response<ErrorResponse> build(ErrorResponse errorResponse) {
		return new Response<>(errorResponse);
	}

	public static <T> Response<T> build(T item) {
		return new Response<>(item);
	}
}
