package com.example.boilerplate.client.goods.dto;

/**
 * [레퍼런스] 파트너별 응답 엔벨로프 패턴.
 *
 * <p>많은 외부 API 가 실제 데이터를 공통 껍데기로 감싸 응답한다:
 * <pre>{@code { "code": "0000", "message": "성공", "data": { ... } } }</pre>
 * 이런 파트너는 응답마다 code/message 를 반복 선언하지 말고, 파트너 패키지 안에
 * 제네릭 엔벨로프를 한 번만 정의해 재사용한다.
 *
 * <p>껍데기 형식은 파트너마다 다르므로 전역 공통화하지 않는다 — 파트너별로 하나씩.
 */
public record GoodsApiEnvelope<T>(
        String code,
        String message,
        T data
) {

    private static final String SUCCESS_CODE = "0000";

    /** 파트너 규격상의 성공 여부. HTTP 200 이어도 body code 가 실패일 수 있어 별도 확인이 필요하다. */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
