package com.example.boilerplate.goods.controller;

import com.example.boilerplate.common.BaseController;
import com.example.boilerplate.common.DataResponse;
import com.example.boilerplate.common.Response;
import com.example.boilerplate.goods.dto.GoodsResponse;
import com.example.boilerplate.goods.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController extends BaseController {

    private final GoodsService goodsService;

    @GetMapping
    public Response<DataResponse<GoodsResponse>> list(Pageable pageable) {
        Page<GoodsResponse> page = goodsService.list(pageable);
        return respond(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
