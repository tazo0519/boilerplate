package com.example.boilerplate.coupon.service;

import com.example.boilerplate.coupon.dto.CouponCreateRequest;
import com.example.boilerplate.coupon.dto.CouponResponse;
import com.example.boilerplate.coupon.dto.CouponUpdateRequest;
import com.example.boilerplate.coupon.entity.Coupon;
import com.example.boilerplate.coupon.repository.CouponRepository;
import com.example.boilerplate.exception.BusinessException;
import com.example.boilerplate.exception.ErrorCode;
import com.example.boilerplate.goods.entity.Goods;
import com.example.boilerplate.goods.repository.GoodsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final GoodsRepository goodsRepository;

    @Transactional
    public CouponResponse create(CouponCreateRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.COUPON_DUPLICATED_CODE, "code=" + request.getCode());
        }
        Goods goods = goodsRepository.findById(request.getGoodsId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND, "goodsId=" + request.getGoodsId()));
        Coupon saved = couponRepository.save(request.toEntity(goods));
        return CouponResponse.from(saved);
    }

    public CouponResponse get(Long id) {
        return CouponResponse.from(findById(id));
    }

    public Page<CouponResponse> list(Pageable pageable) {
        return couponRepository.findAll(pageable).map(CouponResponse::from);
    }

    @Transactional
    public CouponResponse update(Long id, CouponUpdateRequest request) {
        Coupon coupon = findById(id);
        coupon.update(
                request.getName(),
                request.getDiscountAmount(),
                request.getStatus(),
                request.getExpiresAt(),
                request.getRecipientPhone());
        return CouponResponse.from(coupon);
    }

    @Transactional
    public void delete(Long id) {
        couponRepository.delete(findById(id));
    }

    private Coupon findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND, "id=" + id));
    }
}
