package com.example.boilerplate.common.web;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * CORS 설정 — 생성자 바인딩 record 라 바인딩 후 불변이다(런타임 변조 불가).
 * 프로필별로 허용 Origin 을 주입한다(local=localhost, cloud=환경변수).
 *
 * <p>{@code allowedOriginPatterns} 로 등록하므로 서브도메인 패턴({@code https://*.example.com})을
 * 쓸 수 있다. 비어 있으면 CORS 매핑을 등록하지 않아 동일 출처만 허용된다(안전 기본값).
 *
 * @param allowedOrigins   허용할 Origin 패턴 목록. 비어 있으면 CORS 비활성(동일 출처만 허용)
 * @param allowedMethods   허용 HTTP 메서드
 * @param allowedHeaders   허용 요청 헤더. "*" 는 프리플라이트에서 요청된 헤더를 그대로 반영
 * @param exposedHeaders   브라우저에 노출할 응답 헤더(예: 트레이스 헤더)
 * @param allowCredentials 자격증명(쿠키/Authorization) 동반 요청 허용 여부
 * @param maxAge           프리플라이트 캐시 시간(초)
 * @param pathPattern      CORS 를 적용할 경로 패턴
 */
@ConfigurationProperties(prefix = "boilerplate.web.cors")
public record CorsProperties(
        @DefaultValue List<String> allowedOrigins,
        @DefaultValue({"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"}) List<String> allowedMethods,
        @DefaultValue("*") List<String> allowedHeaders,
        @DefaultValue("X-Trace-Id") List<String> exposedHeaders,
        @DefaultValue("true") boolean allowCredentials,
        @DefaultValue("3600") long maxAge,
        @DefaultValue("/**") String pathPattern
) {
}
