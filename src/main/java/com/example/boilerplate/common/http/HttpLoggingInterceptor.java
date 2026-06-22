package com.example.boilerplate.common.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();
        log.info("[REQUEST] method={} uri={}", request.getMethod(), request.getURI());
        try {
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - start;
            log.info("[RESPONSE] status={} duration={}ms", response.getStatusCode().value(), duration);
            return response;
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[RESPONSE ERROR] uri={} duration={}ms message={}", request.getURI(), duration, e.getMessage());
            throw e;
        }
    }
}
