package com.example.boilerplate.client.goods.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoodsApiResponse {

    private Long goodsId;
    private Long originalPrice;
    private Long salePrice;
}
