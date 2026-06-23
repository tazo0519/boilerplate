package com.example.boilerplate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    COMMON_EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 시스템 호출에 실패했습니다."),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_DUPLICATED_CODE(HttpStatus.CONFLICT, "이미 사용 중인 쿠폰 코드입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다."),

    GOODS_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
