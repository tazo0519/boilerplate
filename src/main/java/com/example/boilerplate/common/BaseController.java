package com.example.boilerplate.common;

import java.util.List;
import org.springframework.data.domain.Page;

public class BaseController {

	protected <T> Response<DataResponse<T>> respond(List<T> items) {
		return ResponseBuilder.build(items);
	}

	protected <T> Response<DataResponse<T>> respond(List<T> items, int page, int size, Long totalSize) {
		return ResponseBuilder.build(items, page, size, totalSize);
	}

	// 페이징 응답 공통 처리 — 서비스가 반환한 Page 를 그대로 넘기면 된다.
	protected <T> Response<DataResponse<T>> respond(Page<T> page) {
		return ResponseBuilder.build(page);
	}

	protected <T> Response<T> respond(T item) {
		return ResponseBuilder.build(item);
	}

	protected Response<ErrorResponse> errorRespond(ErrorResponse item) {
		return ResponseBuilder.build(item);
	}
}
