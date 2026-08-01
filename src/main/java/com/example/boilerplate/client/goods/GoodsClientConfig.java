package com.example.boilerplate.client.goods;

import com.example.boilerplate.common.http.HttpClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoodsClientConfig {

    @Bean
    public GoodsClient goodsClient(HttpClientFactory factory,
                                    @Value("${external.goods.base-url}") String baseUrl) {
        return factory.create(GoodsClient.class, baseUrl, "goods-price-api");
    }
}
