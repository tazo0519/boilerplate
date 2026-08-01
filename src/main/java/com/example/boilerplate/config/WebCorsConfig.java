package com.example.boilerplate.config;

import com.example.boilerplate.common.web.CorsProperties;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 매핑을 {@link CorsProperties} 기반으로 등록한다.
 *
 * <p>허용 Origin 이 하나도 없으면 매핑을 등록하지 않는다 → 동일 출처만 허용(안전 기본값).
 * Spring Security 를 도입하면 CORS 는 시큐리티 필터체인에서도 함께 활성화해야 한다.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebCorsConfig implements WebMvcConfigurer {

    private final CorsProperties properties;

    public WebCorsConfig(CorsProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 빈 문자열(예: 환경변수 미설정)로 들어온 항목을 제거해 잘못된 Origin 등록을 방지한다.
        List<String> origins = properties.getAllowedOrigins().stream()
                .filter(o -> o != null && !o.isBlank())
                .toList();
        if (origins.isEmpty()) {
            return;
        }
        registry.addMapping(properties.getPathPattern())
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods(properties.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(properties.getAllowedHeaders().toArray(String[]::new))
                .exposedHeaders(properties.getExposedHeaders().toArray(String[]::new))
                .allowCredentials(properties.isAllowCredentials())
                .maxAge(properties.getMaxAge());
    }
}
