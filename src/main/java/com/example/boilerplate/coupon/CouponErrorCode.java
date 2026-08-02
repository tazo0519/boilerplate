package com.example.boilerplate.coupon;

import com.example.boilerplate.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 쿠폰 도메인 에러 코드 — 도메인 패키지에 두어 공통 enum 수정 없이 추가/삭제한다. */
@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_DUPLICATED_CODE(HttpStatus.CONFLICT, "이미 사용 중인 쿠폰 코드입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
