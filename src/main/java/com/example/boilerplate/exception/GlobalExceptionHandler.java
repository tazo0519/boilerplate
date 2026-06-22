package com.example.boilerplate.exception;

import com.example.boilerplate.common.ErrorResponse;
import com.example.boilerplate.common.Response;
import com.example.boilerplate.common.ResponseBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<ErrorResponse>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("BusinessException: code={} detail={}", errorCode.name(), ex.getDetail());
        ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(ResponseBuilder.build(body));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Response<ErrorResponse>> handleExternalApi(ExternalApiException ex) {
        ErrorCode errorCode = ErrorCode.COMMON_EXTERNAL_API_ERROR;
        log.error("ExternalApiException: target={} detail={}", ex.getTarget(), ex.getDetail(), ex);
        ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(ResponseBuilder.build(body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<ErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("MethodArgumentNotValidException: {} field errors", fieldErrors.size());
        return buildValidationErrorResponse(fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<ErrorResponse>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("ConstraintViolationException: {} field errors", fieldErrors.size());
        return buildValidationErrorResponse(fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<ErrorResponse>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorCode errorCode = ErrorCode.COMMON_INTERNAL_ERROR;
        ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(ResponseBuilder.build(body));
    }

    private ResponseEntity<Response<ErrorResponse>> buildValidationErrorResponse(List<ErrorResponse.FieldError> fieldErrors) {
        ErrorCode errorCode = ErrorCode.COMMON_INVALID_INPUT;
        ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(OffsetDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(ResponseBuilder.build(body));
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
