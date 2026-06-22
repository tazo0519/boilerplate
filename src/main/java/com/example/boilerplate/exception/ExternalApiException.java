package com.example.boilerplate.exception;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {

    private final String target;
    private final String detail;

    public ExternalApiException(String target, String detail) {
        super("External API error: " + target + " (" + detail + ")");
        this.target = target;
        this.detail = detail;
    }

    public ExternalApiException(String target, String detail, Throwable cause) {
        super("External API error: " + target + " (" + detail + ")", cause);
        this.target = target;
        this.detail = detail;
    }
}
