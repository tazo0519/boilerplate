package com.example.boilerplate.goods;

import com.example.boilerplate.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 상품 도메인 에러 코드 — 도메인 패키지에 두어 공통 enum 수정 없이 추가/삭제한다. */
@Getter
@RequiredArgsConstructor
public enum GoodsErrorCode implements ErrorCode {

    GOODS_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
