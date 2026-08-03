package com.example.boilerplate.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.boilerplate.common.web.CorsProperties;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * CORS 가드 회귀 테스트 — 적대적 검증에서 실증된 "'*' + credentials = 모든 사이트의
 * credentialed 요청 조용히 허용" 조합을 부팅 시 거부하는 가드를 고정한다.
 */
class WebCorsConfigTest {

    @Test
    void 와일드카드와_credentials_조합은_부팅_시_거부된다() {
        WebCorsConfig config = new WebCorsConfig(props(List.of("*"), true));

        assertThatThrownBy(() -> config.addCorsMappings(new InspectableCorsRegistry()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allow-credentials");
    }

    @Test
    void 명시_도메인은_credentials_와_함께_정상_등록된다() {
        InspectableCorsRegistry registry = new InspectableCorsRegistry();
        new WebCorsConfig(props(List.of("https://app.example.com"), true)).addCorsMappings(registry);

        CorsConfiguration cors = registry.configurations().get("/**");
        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOriginPatterns()).containsExactly("https://app.example.com");
        assertThat(cors.getAllowCredentials()).isTrue();
    }

    @Test
    void 허용_Origin_이_없으면_CORS_매핑을_등록하지_않는다() {
        InspectableCorsRegistry registry = new InspectableCorsRegistry();
        // 환경변수 미설정 시 빈 문자열이 유입되는 케이스 포함
        new WebCorsConfig(props(List.of(""), true)).addCorsMappings(registry);

        assertThat(registry.configurations()).isEmpty();
    }

    private CorsProperties props(List<String> origins, boolean credentials) {
        return new CorsProperties(origins,
                List.of("GET", "POST"), List.of("*"), List.of("X-Trace-Id"), credentials, 3600L, "/**");
    }

    /** protected getCorsConfigurations() 노출용. */
    static class InspectableCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
