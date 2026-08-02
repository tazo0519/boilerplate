package com.example.boilerplate.goods.service;

import com.example.boilerplate.client.goods.GoodsClient;
import com.example.boilerplate.goods.dto.GoodsResponse;
import com.example.boilerplate.goods.repository.GoodsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoodsService {

    private final GoodsRepository goodsRepository;
    private final GoodsClient goodsClient;

    // 외부 호출 에러 변환은 공통 처리된다 — 4xx/5xx 는 HttpServiceClientsConfig 의 상태 핸들러가
    // ExternalApiException 으로, 연결 실패 등은 GlobalExceptionHandler 가 변환한다.
    public Page<GoodsResponse> list(Pageable pageable) {
        return goodsRepository.findAll(pageable)
                .map(goods -> GoodsResponse.from(goods, goodsClient.fetchPrice(goods.getId())));
    }
}
