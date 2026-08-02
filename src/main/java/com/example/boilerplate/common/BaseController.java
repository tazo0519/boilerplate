package com.example.boilerplate.common;

import java.util.List;
import org.springframework.data.domain.Page;

// 상속 전용 베이스 — 컨트롤러는 이 클래스를 상속해 respond(...) 로만 응답을 만든다.
public abstract class BaseController {

	protected <T> Response<DataResponse<T>> respond(List<T> items) {
		return ResponseBuilder.build(items);
	}

	protected <T> Response<DataResponse<T>> respond(List<T> items, int page, int size, Long totalSize) {
		return ResponseBuilder.build(items, page, size, totalSize);
	}

	// 페이징 응답 공통 처리 — 서비스가 반환한 Page 를 그대로 넘기면 된다.
	protected <T> Response<DataResponse<T>> respond(Page<T> page) {
		return respond(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
	}

	protected <T> Response<T> respond(T item) {
		return ResponseBuilder.build(item);
	}

	protected Response<ErrorResponse> errorRespond(ErrorResponse item) {
		return ResponseBuilder.build(item);
	}
}
