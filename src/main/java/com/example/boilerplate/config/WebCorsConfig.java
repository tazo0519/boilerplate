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
 * {@code allowedOriginPatterns} 로 등록하므로 {@code allow-credentials=true} 와 함께
 * 와일드카드 패턴(예: {@code *}, {@code https://*.example.com})도 안전하게 쓸 수 있다
 * (자격증명+{@code allowedOrigins("*")} 조합의 런타임 예외를 회피).
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
        List<String> origins = properties.allowedOrigins().stream()
                .filter(o -> o != null && !o.isBlank())
                .toList();
        if (origins.isEmpty()) {
            return;
        }
        // 가드: "*" + 자격증명 허용은 모든 사이트의 credentialed 교차출처 요청을 조용히 허용하는
        // 조합이라 부팅 단계에서 거부한다(fail-fast). 서브도메인 패턴(https://*.example.com)은 허용.
        if (properties.allowCredentials() && origins.contains("*")) {
            throw new IllegalStateException(
                    "CORS 설정 오류: allow-credentials=true 상태에서 allowed-origins 에 \"*\" 를 쓸 수 없습니다. "
                            + "명시 도메인(또는 https://*.example.com 형태의 패턴)을 지정하거나 allow-credentials 를 끄세요.");
        }
        // allowedOriginPatterns: allow-credentials=true 와 함께 "*"/서브도메인 패턴을 허용한다.
        // (allowedOrigins 는 credentials 동반 시 "*" 를 넣으면 요청 처리 중 예외를 던진다.)
        registry.addMapping(properties.pathPattern())
                .allowedOriginPatterns(origins.toArray(String[]::new))
                .allowedMethods(properties.allowedMethods().toArray(String[]::new))
                .allowedHeaders(properties.allowedHeaders().toArray(String[]::new))
                .exposedHeaders(properties.exposedHeaders().toArray(String[]::new))
                .allowCredentials(properties.allowCredentials())
                .maxAge(properties.maxAge());
    }
}
