package com.example.boilerplate.common.http;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class TraceIdPropagationInterceptor implements ClientHttpRequestInterceptor {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String REQUEST_HEADER = "X-Request-Id";

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !request.getHeaders().containsHeader(REQUEST_HEADER)) {
            request.getHeaders().set(REQUEST_HEADER, traceId);
        }
        return execution.execute(request, body);
    }
}
