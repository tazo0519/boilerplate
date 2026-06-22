package com.example.boilerplate.config;

import com.example.boilerplate.common.http.HttpLoggingInterceptor;
import com.example.boilerplate.common.http.TraceIdPropagationInterceptor;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer(
            TraceIdPropagationInterceptor traceIdInterceptor,
            HttpLoggingInterceptor loggingInterceptor) {
        return builder -> builder
                .requestInterceptor(traceIdInterceptor)
                .requestInterceptor(loggingInterceptor);
    }
}
