package com.example.boilerplate.config;

import com.example.boilerplate.client.goods.GoodsClient;
import com.example.boilerplate.exception.ExternalApiException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * 외부 연동 클라이언트 등록 — Spring Framework 7 의 HTTP Service Group 을 사용한다.
 *
 * <p>새 파트너 연동 추가 절차 (Config 클래스·팩토리 코드 불필요):
 * <ol>
 *   <li>{@code client/{대상}/} 에 {@code @HttpExchange} 인터페이스 선언</li>
 *   <li>아래에 {@code @ImportHttpServices(group = "{대상}", types = XxxClient.class)} 한 줄 추가
 *       (@Repeatable — 그룹마다 반복 선언)</li>
 *   <li>yaml 에 {@code spring.http.serviceclient.{대상}.base-url} 설정
 *       (그룹별 default-header/타임아웃도 같은 위치에서 — 전역 {@code spring.http.clients.*} 는 fallback)</li>
 * </ol>
 *
 * <p>공통으로 자동 적용되는 것:
 * <ul>
 *   <li>trace/logging 인터셉터 — RestClientConfig 의 {@code RestClientCustomizer} 를 Boot 이
 *       모든 그룹 클라이언트에 적용한다(RestClientCustomizerHttpServiceGroupConfigurer).</li>
 *   <li>에러 변환 — 아래 configurer 가 4xx/5xx 를 {@link ExternalApiException} 으로 변환,
 *       연결 실패 등 I/O 오류는 GlobalExceptionHandler 가 처리한다.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "goods", types = GoodsClient.class)
public class HttpServiceClientsConfig {

    @Bean
    public RestClientHttpServiceGroupConfigurer externalApiErrorConfigurer() {
        // 모든 그룹에 공통 에러 변환 적용. 그룹명이 에러의 target 식별자가 된다.
        // 응답 본문은 민감정보 가능성이 있어 포함하지 않는다 — 상태/메서드/URI 만.
        return groups -> groups.forEachClient((group, builder) ->
                builder.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new ExternalApiException(group.name(),
                            "HTTP " + response.getStatusCode().value()
                                    + " " + request.getMethod() + " " + request.getURI());
                }));
    }
}
