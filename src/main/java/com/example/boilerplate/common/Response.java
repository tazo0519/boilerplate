package com.example.boilerplate.common;

import lombok.Getter;

@Getter
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
