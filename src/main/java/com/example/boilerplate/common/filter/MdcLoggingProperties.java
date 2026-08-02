package com.example.boilerplate.common.filter;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 액세스 로그(MdcLoggingFilter) 설정 — 생성자 바인딩 record 라 바인딩 후 불변이다.
 *
 * @param excludePaths 액세스 로그·traceId 처리에서 제외할 경로 패턴(PathPattern 문법).
 *                     기본값은 ALB/ECS 가 수 초마다 호출하는 헬스체크 — 로그 노이즈 제거 목적.
 */
@ConfigurationProperties(prefix = "boilerplate.logging")
public record MdcLoggingProperties(
        @DefaultValue({"/actuator/health", "/actuator/health/**"}) List<String> excludePaths
) {
}
