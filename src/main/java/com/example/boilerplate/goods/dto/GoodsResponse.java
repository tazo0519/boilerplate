package com.example.boilerplate.goods.dto;

import com.example.boilerplate.client.goods.dto.GoodsApiResponse;
import com.example.boilerplate.goods.entity.Goods;
import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GoodsResponse from(Goods goods, GoodsApiResponse priceInfo) {
        return GoodsResponse.builder()
                .id(goods.getId())
                .code(goods.getCode())
                .name(goods.getName())
                .description(goods.getDescription())
                .originalPrice(priceInfo != null ? priceInfo.getOriginalPrice() : null)
                .salePrice(priceInfo != null ? priceInfo.getSalePrice() : null)
                .createdAt(goods.getCreatedAt())
                .updatedAt(goods.getUpdatedAt())
                .build();
    }
}
