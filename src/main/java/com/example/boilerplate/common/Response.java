package com.example.boilerplate.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private T content;
    private ErrorResponse errors;

    // 생성은 ResponseBuilder/BaseController 를 통해서만 한다(package-private).
    // 직렬화 전용 모델 — creator 가 없어 역직렬화는 불가(의도된 제약).
    Response(ErrorResponse errors) {
        this.errors = errors;
    }

    Response(T content) {
        this.content = content;
    }
}
