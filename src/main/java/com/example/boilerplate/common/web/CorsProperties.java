package com.example.boilerplate.common.web;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS 설정. 프로필별로 허용 Origin 을 주입한다(local=localhost, cloud=환경변수).
 *
 * <p>{@code allow-credentials=true} 이면 Origin 에 와일드카드(*)를 쓸 수 없으므로,
 * {@code allowed-origins} 는 반드시 명시 도메인 목록으로 설정한다. 비어 있으면 CORS 매핑을
 * 등록하지 않아 동일 출처만 허용된다(안전 기본값).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "boilerplate.web.cors")
public class CorsProperties {

    /** 허용할 Origin 목록. 비어 있으면 CORS 비활성(동일 출처만 허용). 와일드카드(*) 불가. */
    private List<String> allowedOrigins = List.of();

    /** 허용 HTTP 메서드. */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /** 허용 요청 헤더. "*" 는 프리플라이트에서 요청된 헤더를 그대로 반영한다. */
    private List<String> allowedHeaders = List.of("*");

    /** 브라우저에 노출할 응답 헤더(예: 트레이스 헤더). */
    private List<String> exposedHeaders = List.of("X-Trace-Id");

    /** 자격증명(쿠키/Authorization) 동반 요청 허용 여부. true 면 Origin 와일드카드 불가. */
    private boolean allowCredentials = true;

    /** 프리플라이트 캐시 시간(초). */
    private long maxAge = 3600;

    /** CORS 를 적용할 경로 패턴. */
    private String pathPattern = "/**";
}
