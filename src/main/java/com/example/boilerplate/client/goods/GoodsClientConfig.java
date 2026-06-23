package com.example.boilerplate.client.goods;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class GoodsClientConfig {

    @Bean
    public GoodsClient goodsClient(RestClient.Builder builder,
                                    @Value("${external.goods.base-url}") String baseUrl) {
        RestClient restClient = builder.baseUrl(baseUrl).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(GoodsClient.class);
    }
}
