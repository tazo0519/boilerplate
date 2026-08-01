package com.example.boilerplate.client.goods.dto;

/**
 * [레퍼런스] POST 요청 body DTO 예시 — 요청 DTO 도 record 로 선언한다.
 */
public record GoodsQuoteApiRequest(
        Long goodsId,
        int quantity
) {
}
