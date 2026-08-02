package com.example.boilerplate.exception;

import com.example.boilerplate.common.ErrorResponse;
import com.example.boilerplate.common.Response;
import com.example.boilerplate.common.ResponseBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 전역 예외 처리 — 모든 에러 응답을 단일 계약({@code Response.errors})으로 수렴시킨다.
 *
 * <p>{@link ResponseEntityExceptionHandler} 를 상속해 프레임워크 예외(존재하지 않는 경로 404,
 * 잘못된 body 400, 허용 안 된 메서드 405 등 약 20종)도 <b>올바른 상태코드 + 동일 봉투</b>로
 * 응답한다. 상속 없이는 catch-all 로 흘러 전부 500 으로 둔갑했다(부팅 프로브로 실측).
 *
 * <p>로깅 규약: 4xx(클라이언트 잘못)는 warn·스택 없음, 5xx(서버 잘못)는 error·스택 포함 —
 * 모니터링 알람이 서버 결함에만 반응하게 한다. 모든 에러 본문에는 traceId 가 포함되어
 * 사용자 문의 시 서버 로그를 바로 찾을 수 있다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId"; // MdcLoggingFilter 가 채운다

    // ==================== 프레임워크 예외 (부모의 모든 핸들러가 여기로 수렴) ====================

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        // 부모가 만든 ProblemDetail body 는 버리고 우리 봉투로 통일한다. 상태코드는 프레임워크 판단을 보존.
        logByStatus(statusCode, ex);
        return ResponseEntity.status(statusCode).headers(headers)
                .body(ResponseBuilder.build(buildBody(mapFrameworkStatus(statusCode), null)));
    }

    // 검증 실패는 fieldErrors 를 포함해야 하므로 별도 오버라이드
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("MethodArgumentNotValidException: {} field errors", fieldErrors.size());
        ErrorCode errorCode = CommonErrorCode.COMMON_INVALID_INPUT;
        return ResponseEntity.status(errorCode.getStatus()).headers(headers)
                .body(ResponseBuilder.build(buildBody(errorCode, fieldErrors)));
    }

    // ==================== 애플리케이션 예외 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<ErrorResponse>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("BusinessException: code={} detail={}", errorCode.getCode(), ex.getDetail());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ResponseBuilder.build(buildBody(errorCode, null)));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Response<ErrorResponse>> handleExternalApi(ExternalApiException ex) {
        ErrorCode errorCode = CommonErrorCode.COMMON_EXTERNAL_API_ERROR;
        log.error("ExternalApiException: target={} detail={}", ex.getTarget(), ex.getDetail(), ex);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ResponseBuilder.build(buildBody(errorCode, null)));
    }

    // 상태코드 에러(4xx/5xx)는 HttpServiceClientsConfig 가 ExternalApiException 으로 변환하므로,
    // 여기 도달하는 것은 연결 실패·타임아웃 등 I/O 계열(ResourceAccessException 등)이다.
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Response<ErrorResponse>> handleRestClient(RestClientException ex) {
        ErrorCode errorCode = CommonErrorCode.COMMON_EXTERNAL_API_ERROR;
        log.error("RestClientException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ResponseBuilder.build(buildBody(errorCode, null)));
    }

    // 존재하지 않는 필드로 정렬/조회 요청(?sort=없는필드) 시 Spring Data 가 던진다 → 400.
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Response<ErrorResponse>> handlePropertyReference(PropertyReferenceException ex) {
        ErrorCode errorCode = CommonErrorCode.COMMON_BAD_REQUEST;
        log.warn("PropertyReferenceException: {}", ex.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ResponseBuilder.build(buildBody(errorCode, null)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<ErrorResponse>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("ConstraintViolationException: {} field errors", fieldErrors.size());
        ErrorCode errorCode = CommonErrorCode.COMMON_INVALID_INPUT;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ResponseBuilder.build(buildBody(errorCode, fieldErrors)));
    }

    // 최후의 안전망 — 여기 도달한 것만이 진짜 미처리 서버 결함이다(5xx + 스택).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<ErrorResponse>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorCode errorCode = CommonErrorCode.COMMON_INTERNAL_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ResponseBuilder.build(buildBody(errorCode, null)));
    }

    // ==================== 내부 헬퍼 ====================

    // HTTP 상태(프레임워크 판단)를 우리 코드 체계로 근사 매핑한다. 응답 상태코드 자체는 보존된다.
    private ErrorCode mapFrameworkStatus(HttpStatusCode status) {
        if (HttpStatus.NOT_FOUND.isSameCodeAs(status)) {
            return CommonErrorCode.COMMON_NOT_FOUND;
        }
        if (HttpStatus.METHOD_NOT_ALLOWED.isSameCodeAs(status)) {
            return CommonErrorCode.COMMON_METHOD_NOT_ALLOWED;
        }
        if (HttpStatus.UNSUPPORTED_MEDIA_TYPE.isSameCodeAs(status)) {
            return CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE;
        }
        if (status.is4xxClientError()) {
            return CommonErrorCode.COMMON_BAD_REQUEST;
        }
        return CommonErrorCode.COMMON_INTERNAL_ERROR;
    }

    private void logByStatus(HttpStatusCode status, Exception ex) {
        if (status.is5xxServerError()) {
            log.error("Framework exception: status={} type={}", status.value(), ex.getClass().getSimpleName(), ex);
        } else {
            log.warn("Framework exception: status={} type={} message={}",
                    status.value(), ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    private ErrorResponse buildBody(ErrorCode errorCode, List<ErrorResponse.FieldError> fieldErrors) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(MDC.get(TRACE_ID_KEY))
                .timestamp(OffsetDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
    }

    private ErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .reason(fieldError.getDefaultMessage())
                .build();
    }

    private ErrorResponse.FieldError toFieldError(ConstraintViolation<?> violation) {
        return ErrorResponse.FieldError.builder()
                .field(violation.getPropertyPath().toString())
                .reason(violation.getMessage())
                .build();
    }
}
