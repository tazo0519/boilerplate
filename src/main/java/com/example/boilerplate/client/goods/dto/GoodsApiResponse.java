package com.example.boilerplate.client.goods.dto;

/**
 * 외부 응답 DTO 는 record 로 선언한다 — 받아서 읽기만 하는 불변 데이터.
 * (Jackson 이 record 를 그대로 역직렬화한다)
 */
public record GoodsApiResponse(
        Long goodsId,
        Long originalPrice,
        Long salePrice
) {
}
