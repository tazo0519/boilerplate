package com.example.boilerplate.common;

import java.util.List;

// 정적 유틸 — 인스턴스화·상속을 제한한다.
public final class ResponseBuilder {

	private ResponseBuilder() {
	}

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
