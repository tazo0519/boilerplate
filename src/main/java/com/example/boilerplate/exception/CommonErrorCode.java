package com.example.boilerplate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** 전 도메인 공통 에러 코드. 도메인 고유 코드는 여기가 아니라 각 도메인 패키지의 enum 에 추가한다. */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    COMMON_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "요청한 응답 형식으로는 제공할 수 없습니다."),
    COMMON_UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 요청 형식입니다."),
    COMMON_EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 시스템 호출에 실패했습니다."),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    /**
     * HTTP 상태코드 → 공통 코드 근사 매핑 — 프레임워크/서블릿 컨테이너가 상태코드만 알려주는
     * 경로(ResponseEntityExceptionHandler 수렴, ERROR 디스패치)에서 사용한다.
     */
    public static CommonErrorCode fromStatus(HttpStatusCode status) {
        return switch (HttpStatus.resolve(status.value())) {
            case NOT_FOUND -> COMMON_NOT_FOUND;
            case METHOD_NOT_ALLOWED -> COMMON_METHOD_NOT_ALLOWED;
            case NOT_ACCEPTABLE -> COMMON_NOT_ACCEPTABLE;
            case UNSUPPORTED_MEDIA_TYPE -> COMMON_UNSUPPORTED_MEDIA_TYPE;
            case null, default -> status.is4xxClientError() ? COMMON_BAD_REQUEST : COMMON_INTERNAL_ERROR;
        };
    }

    @Override
    public String getCode() {
        return name();
    }
}
