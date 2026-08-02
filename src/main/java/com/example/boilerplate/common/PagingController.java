package com.example.boilerplate.common;

import org.springframework.data.domain.Page;

/**
 * 페이징 응답 확장 지점 — 목록 API 를 제공하는 컨트롤러는 이 클래스를 상속한다.
 *
 * <p>응답 계약 코어(BaseController/Response/DataResponse/ResponseBuilder/ErrorResponse)는
 * 수정하지 않고(닫힘), 편의 기능은 이렇게 별도 확장 클래스로 제공한다(열림).
 * 내부적으로 코어의 기존 public API({@code respond(items, page, size, totalSize)})에
 * 위임만 하므로 응답 형태는 동일하다.
 */
public abstract class PagingController extends BaseController {

	// 서비스가 반환한 Page 를 그대로 넘기면 페이지 메타데이터를 공통으로 언패킹한다.
	protected <T> Response<DataResponse<T>> respond(Page<T> page) {
		return respond(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
	}
}
