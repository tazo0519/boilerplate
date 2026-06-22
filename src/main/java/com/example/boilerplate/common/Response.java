package com.example.boilerplate.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private T content;
    private ErrorResponse errors;

    public Response() {
    }

    public Response(ErrorResponse errors) {
        this.errors = errors;
    }

    public Response(T content) {
        this.content = content;
    }
}
