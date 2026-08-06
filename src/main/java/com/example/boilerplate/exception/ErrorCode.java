package com.example.boilerplate.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 계약(확장 지점) — 공통 코드는 {@link CommonErrorCode}, 도메인 코드는 각 도메인
 * 패키지에서 이 인터페이스를 구현한 enum 으로 정의한다(작성 패턴: 테스트 픽스처 SampleErrorCode).
 *
 * <p>새 도메인의 에러 코드를 추가할 때 이 파일이나 공통 enum 을 수정하지 않는다 —
 * 도메인 패키지에 enum 을 새로 만들면 된다(도메인 삭제 시 에러 코드도 함께 사라진다).
 */
public interface ErrorCode {

    HttpStatus getStatus();

    String getMessage();

    /** 응답 {@code code} 필드로 나가는 식별자. enum 구현체는 {@code name()} 을 반환한다. */
    String getCode();
}
