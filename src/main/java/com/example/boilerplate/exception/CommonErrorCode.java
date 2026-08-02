package com.example.boilerplate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 전 도메인 공통 에러 코드. 도메인 고유 코드는 여기가 아니라 각 도메인 패키지의 enum 에 추가한다. */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    COMMON_EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 시스템 호출에 실패했습니다."),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
