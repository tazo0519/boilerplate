package com.example.boilerplate.exception;

import com.example.boilerplate.common.ErrorResponse;
import com.example.boilerplate.common.ResponseBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 전역 예외 처리 — 모든 에러 응답을 단일 계약({@code Response.errors})으로 수렴시킨다.
 *
 * <p>구조: 핸들러는 "예외 → ErrorCode(+fieldErrors)" 매핑만 선언하고, 응답 조립·로깅은
 * 전부 단일 경로 {@link #respond} 가 수행한다. 새 예외 처리가 필요하면 핸들러 한 줄만 추가한다.
 *
 * <p>{@link ResponseEntityExceptionHandler} 를 상속해 프레임워크 예외(존재하지 않는 경로 404,
 * 잘못된 body 400, 허용 안 된 메서드 405 등 약 20종)도 <b>올바른 상태코드 + 동일 봉투</b>로
 * 응답한다. 상속 없이는 catch-all 로 흘러 전부 500 으로 둔갑한다(부팅 프로브로 실측).
 *
 * <p>로깅 규약(상태코드 기준으로 일괄 적용): 4xx = warn·스택 없음(클라이언트 잘못),
 * 5xx = error·스택 포함(서버 잘못) — 모니터링 알람이 서버 결함에만 반응한다.
 * 모든 에러 본문에는 traceId 가 포함되어 사용자 문의 시 서버 로그를 바로 찾을 수 있다.
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
        return respond(mapStatus(statusCode), statusCode, headers, null, ex);
    }

    // 검증 실패는 fieldErrors 를 포함해야 하므로 별도 오버라이드
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respond(CommonErrorCode.COMMON_INVALID_INPUT, status, headers,
                toFieldErrors(ex.getBindingResult()), ex);
    }

    // ==================== 애플리케이션 예외 (매핑 선언만 — 조립·로깅은 respond 가) ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException ex) {
        return respond(ex.getErrorCode(), ex);
    }

    // 4xx/5xx 응답은 HttpServiceClientsConfig 가 ExternalApiException 으로 변환하고,
    // 연결 실패·타임아웃 등 I/O 계열은 RestClientException 으로 도달한다 — 결과는 동일한 502.
    @ExceptionHandler({ExternalApiException.class, RestClientException.class})
    public ResponseEntity<Object> handleExternalApi(Exception ex) {
        return respond(CommonErrorCode.COMMON_EXTERNAL_API_ERROR, ex);
    }

    // 존재하지 않는 필드로 정렬/조회 요청(?sort=없는필드) 시 Spring Data 가 던진다 → 400.
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Object> handlePropertyReference(PropertyReferenceException ex) {
        return respond(CommonErrorCode.COMMON_BAD_REQUEST, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorCode errorCode = CommonErrorCode.COMMON_INVALID_INPUT;
        return respond(errorCode, errorCode.getStatus(), null, toFieldErrors(ex.getConstraintViolations()), ex);
    }

    // 최후의 안전망 — 여기 도달한 것만이 진짜 미처리 서버 결함이다(5xx + 스택).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnhandled(Exception ex) {
        return respond(CommonErrorCode.COMMON_INTERNAL_ERROR, ex);
    }

    // ==================== 단일 응답 경로 ====================

    /** 상태코드를 ErrorCode 가 결정하는 일반 케이스. */
    private ResponseEntity<Object> respond(ErrorCode errorCode, Exception ex) {
        return respond(errorCode, errorCode.getStatus(), null, null, ex);
    }

    /** 모든 에러 응답이 통과하는 유일한 조립·로깅 지점. */
    private ResponseEntity<Object> respond(ErrorCode errorCode, HttpStatusCode status,
            HttpHeaders headers, List<ErrorResponse.FieldError> fieldErrors, Exception ex) {
        logByStatus(status, errorCode, summaryOf(ex, fieldErrors), ex);
        ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(MDC.get(TRACE_ID_KEY))
                .timestamp(OffsetDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).headers(headers).body(ResponseBuilder.build(body));
    }

    // ==================== 목적별 헬퍼 ====================

    /** HTTP 상태(프레임워크 판단) → 우리 코드 체계 근사 매핑. 응답 상태코드 자체는 보존된다. */
    private ErrorCode mapStatus(HttpStatusCode status) {
        return switch (HttpStatus.resolve(status.value())) {
            case NOT_FOUND -> CommonErrorCode.COMMON_NOT_FOUND;
            case METHOD_NOT_ALLOWED -> CommonErrorCode.COMMON_METHOD_NOT_ALLOWED;
            case UNSUPPORTED_MEDIA_TYPE -> CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE;
            case null, default -> status.is4xxClientError()
                    ? CommonErrorCode.COMMON_BAD_REQUEST
                    : CommonErrorCode.COMMON_INTERNAL_ERROR;
        };
    }

    /** 로깅 정책 — 예외 종류가 아니라 응답 상태코드가 심각도를 결정한다. */
    private void logByStatus(HttpStatusCode status, ErrorCode errorCode, String summary, Exception ex) {
        if (status.is5xxServerError()) {
            log.error("{} code={} {}", ex.getClass().getSimpleName(), errorCode.getCode(), summary, ex);
        } else {
            log.warn("{} code={} {}", ex.getClass().getSimpleName(), errorCode.getCode(), summary);
        }
    }

    /** 로그 한 줄 요약 — 검증 실패는 장황한 원본 메시지 대신 필드 오류 개수로. */
    private String summaryOf(Exception ex, List<ErrorResponse.FieldError> fieldErrors) {
        return fieldErrors != null ? fieldErrors.size() + " field errors" : ex.getMessage();
    }

    private List<ErrorResponse.FieldError> toFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(f -> ErrorResponse.FieldError.builder()
                        .field(f.getField())
                        .reason(f.getDefaultMessage())
                        .build())
                .toList();
    }

    private List<ErrorResponse.FieldError> toFieldErrors(Set<ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(v -> ErrorResponse.FieldError.builder()
                        .field(v.getPropertyPath().toString())
                        .reason(v.getMessage())
                        .build())
                .toList();
    }
}
