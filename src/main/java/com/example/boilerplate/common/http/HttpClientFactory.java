package com.example.boilerplate.common.http;

import com.example.boilerplate.exception.ExternalApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 선언형 HTTP 클라이언트({@code @HttpExchange} 인터페이스) 공통 팩토리.
 *
 * <p>클라이언트마다 RestClient + HttpServiceProxyFactory 조립 코드를 반복하지 않도록,
 * 새 클라이언트는 설정 클래스에서 아래 한 줄로 생성한다:
 * <pre>{@code
 * @Bean
 * GoodsClient goodsClient(HttpClientFactory factory,
 *                         @Value("${external.goods.base-url}") String baseUrl) {
 *     return factory.create(GoodsClient.class, baseUrl, "goods-price-api");
 * }
 * }</pre>
 *
 * <p>공통으로 적용되는 것:
 * <ul>
 *   <li>trace/logging 인터셉터 — 자동구성 {@link RestClient.Builder} 에
 *       {@code RestClientCustomizer} 로 이미 등록되어 있다(RestClientConfig).</li>
 *   <li>타임아웃 — {@code spring.http.clients.*} 공통 설정을 따른다.</li>
 *   <li>에러 변환 — 4xx/5xx 응답은 {@link ExternalApiException} 으로 변환되어
 *       GlobalExceptionHandler 가 일관 응답(COMMON_EXTERNAL_API_ERROR)으로 처리한다.
 *       (연결 실패 등 I/O 오류는 RestClientException 으로 던져지며, 전역 핸들러에서 동일하게 변환)</li>
 * </ul>
 */
@Component
public class HttpClientFactory {

    private final RestClient.Builder builder;

    public HttpClientFactory(RestClient.Builder builder) {
        this.builder = builder;
    }

    /**
     * @param clientType {@code @HttpExchange} 선언형 인터페이스
     * @param baseUrl    대상 시스템 base URL (설정으로 주입)
     * @param targetName 에러/로그에서 대상 시스템을 식별할 이름 (예: "goods-price-api")
     */
    public <T> T create(Class<T> clientType, String baseUrl, String targetName) {
        RestClient restClient = builder.clone()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    // 응답 본문은 민감정보 가능성이 있어 포함하지 않는다 — 상태/메서드/URI 만.
                    throw new ExternalApiException(targetName,
                            "HTTP " + response.getStatusCode().value()
                                    + " " + request.getMethod() + " " + request.getURI());
                })
                .build();
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(clientType);
    }
}
