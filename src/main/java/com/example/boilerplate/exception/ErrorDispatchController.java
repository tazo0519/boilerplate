package com.example.boilerplate.exception;

import com.example.boilerplate.common.ErrorResponse;
import com.example.boilerplate.common.Response;
import com.example.boilerplate.common.ResponseBuilder;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ERROR 디스패치(/error)도 봉투 계약으로 수렴시킨다 — Boot 의 BasicErrorController 를 대체.
 *
 * <p>대상: MVC/advice 에 도달하지 못하고 서블릿 컨테이너의 sendError 로 빠진 에러
 * (필터에서 던져진 예외 등)와 /error 직접 호출. BasicErrorController 는 다른 형태의 JSON 을
 * 반환하고 에러 컨텍스트 없는 직접 호출을 가짜 500 으로 보고했다(적대적 검증 실측).
 *
 * <p>한계(문서화): Tomcat 이 요청 파싱 자체에 실패하는 경우(잘못된 percent-encoding URI,
 * 헤더 크기 초과 등)는 앱 코드에 도달하지 않아 컨테이너 기본 응답으로 남는다. 이 경로까지
 * 통제하려면 Tomcat ErrorReportValve 커스터마이즈가 필요하다.
 *
 * <p>traceId: 이 경로는 MdcLoggingFilter 이전(또는 이후 정리된 뒤)에 실행될 수 있어
 * MDC 가 비어 있으면 traceId 필드가 생략된다.
 */
@Slf4j
@RestController
class ErrorDispatchController implements ErrorController {

    private static final String TRACE_ID_KEY = "traceId";

    @RequestMapping("${server.error.path:${error.path:/error}}")
    ResponseEntity<Response<ErrorResponse>> handleError(HttpServletRequest request) {
        HttpStatusCode status = resolveStatus(request);
        ErrorCode errorCode = CommonErrorCode.fromStatus(status);
        log.warn("Error dispatch: status={} uri={}", status.value(),
                request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(MDC.get(TRACE_ID_KEY))
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseBuilder.build(body));
    }

    private HttpStatusCode resolveStatus(HttpServletRequest request) {
        if (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) instanceof Integer code && code >= 400) {
            return HttpStatusCode.valueOf(code);
        }
        // 에러 컨텍스트 없이 /error 를 직접 호출한 경우 — 가짜 500 대신 404 로 응답한다.
        return HttpStatus.NOT_FOUND;
    }
}
