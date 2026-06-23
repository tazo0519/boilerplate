package com.example.boilerplate.coupon.dto;

import com.example.boilerplate.common.mask.MaskType;
import com.example.boilerplate.common.mask.Masked;
import com.example.boilerplate.coupon.entity.Coupon;
import com.example.boilerplate.coupon.entity.CouponStatus;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CouponResponse {

    private Long id;
    private String code;
    private String name;
    private Long discountAmount;
    private CouponStatus status;
    private OffsetDateTime expiresAt;

    @Masked(MaskType.PHONE)
    private String recipientPhone;

    private Long goodsId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .discountAmount(coupon.getDiscountAmount())
                .status(coupon.getStatus())
                .expiresAt(coupon.getExpiresAt())
                .recipientPhone(coupon.getRecipientPhone())
                .goodsId(coupon.getGoods().getId())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}
