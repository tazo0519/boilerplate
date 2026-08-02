package com.example.boilerplate.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private T content;
    private ErrorResponse errors;

    // 생성은 ResponseBuilder/BaseController 를 통해서만 한다(package-private).
    Response() {
    }

    Response(ErrorResponse errors) {
        this.errors = errors;
    }

    Response(T content) {
        this.content = content;
    }
}
