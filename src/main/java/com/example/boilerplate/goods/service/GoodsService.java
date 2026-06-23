package com.example.boilerplate.goods.service;

import com.example.boilerplate.client.goods.GoodsClient;
import com.example.boilerplate.client.goods.dto.GoodsApiResponse;
import com.example.boilerplate.exception.ExternalApiException;
import com.example.boilerplate.goods.dto.GoodsResponse;
import com.example.boilerplate.goods.repository.GoodsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoodsService {

    private static final String TARGET = "goods-price-api";

    private final GoodsRepository goodsRepository;
    private final GoodsClient goodsClient;

    public Page<GoodsResponse> list(Pageable pageable) {
        return goodsRepository.findAll(pageable)
                .map(goods -> GoodsResponse.from(goods, fetchPrice(goods.getId())));
    }

    private GoodsApiResponse fetchPrice(Long goodsId) {
        try {
            return goodsClient.fetchPrice(goodsId);
        } catch (RestClientException e) {
            throw new ExternalApiException(TARGET, "fetch price failed goodsId=" + goodsId, e);
        }
    }
}
