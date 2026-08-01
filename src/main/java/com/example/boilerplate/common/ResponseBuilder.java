package com.example.boilerplate.common;

import java.util.List;
import org.springframework.data.domain.Page;

public class ResponseBuilder {

	public static <T> Response<DataResponse<T>> build(List<T> items) {
		return new Response<>(new DataResponse<>(items));
	}

	public static <T> Response<DataResponse<T>> build(List<T> items, Integer page, Integer size, Long totalSize) {
		return new Response<>(new DataResponse<>(items, page, size, totalSize));
	}

	// Spring Data Page 를 그대로 받아 페이지 메타데이터(number/size/totalElements)를 공통으로 언패킹한다.
	// 컨트롤러마다 getContent()/getNumber()... 를 반복하지 않도록 한다.
	public static <T> Response<DataResponse<T>> build(Page<T> page) {
		return new Response<>(new DataResponse<>(
				page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements()));
	}

	public static Response<ErrorResponse> build(ErrorResponse errorResponse) {
		return new Response<>(errorResponse);
	}

	public static <T> Response<T> build(T item) {
		return new Response<>(item);
	}
}
