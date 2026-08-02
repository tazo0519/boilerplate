package com.example.boilerplate.common;

import java.util.List;

public class BaseController {

	protected <T> Response<DataResponse<T>> respond(List<T> items) {
		return ResponseBuilder.build(items);
	}

	protected <T> Response<DataResponse<T>> respond(List<T> items, int page, int size, Long totalSize) {
		return ResponseBuilder.build(items, page, size, totalSize);
	}

	protected <T> Response<T> respond(T item) {
		return ResponseBuilder.build(item);
	}

	protected Response<ErrorResponse> errorRespond(ErrorResponse item) {
		return ResponseBuilder.build(item);
	}
}
