package com.example.boilerplate.client.goods;

import com.example.boilerplate.client.goods.dto.GoodsApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface GoodsClient {

    @GetExchange("/goods/{goodsId}/price")
    GoodsApiResponse fetchPrice(@PathVariable Long goodsId);
}
