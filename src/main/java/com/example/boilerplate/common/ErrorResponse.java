package com.example.boilerplate.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

	private String errorCode;
	private String errorDescription;
}
