package com.example.boilerplate.coupon.controller;

import com.example.boilerplate.common.BaseController;
import com.example.boilerplate.common.DataResponse;
import com.example.boilerplate.common.Response;
import com.example.boilerplate.coupon.dto.CouponCreateRequest;
import com.example.boilerplate.coupon.dto.CouponResponse;
import com.example.boilerplate.coupon.dto.CouponUpdateRequest;
import com.example.boilerplate.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController extends BaseController {

    private final CouponService couponService;

    @PostMapping
    public Response<CouponResponse> create(@Valid @RequestBody CouponCreateRequest request) {
        return respond(couponService.create(request));
    }

    @GetMapping("/{id}")
    public Response<CouponResponse> get(@PathVariable Long id) {
        return respond(couponService.get(id));
    }

    @GetMapping
    public Response<DataResponse<CouponResponse>> list(Pageable pageable) {
        Page<CouponResponse> page = couponService.list(pageable);
        return respond(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @PutMapping("/{id}")
    public Response<CouponResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody CouponUpdateRequest request) {
        return respond(couponService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
