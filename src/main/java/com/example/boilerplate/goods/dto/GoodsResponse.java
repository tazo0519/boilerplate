package com.example.boilerplate.goods.dto;

import com.example.boilerplate.client.goods.dto.GoodsApiResponse;
import com.example.boilerplate.goods.entity.Goods;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GoodsResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Long originalPrice;
    private Long salePrice;
    private Instant createdAt;
    private Instant updatedAt;

    public static GoodsResponse from(Goods goods, GoodsApiResponse priceInfo) {
        return GoodsResponse.builder()
                .id(goods.getId())
                .code(goods.getCode())
                .name(goods.getName())
                .description(goods.getDescription())
                .originalPrice(priceInfo != null ? priceInfo.originalPrice() : null)
                .salePrice(priceInfo != null ? priceInfo.salePrice() : null)
                .createdAt(goods.getCreatedAt())
                .updatedAt(goods.getUpdatedAt())
                .build();
    }
}
