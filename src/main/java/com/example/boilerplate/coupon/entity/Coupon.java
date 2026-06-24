package com.example.boilerplate.coupon.entity;

import com.example.boilerplate.common.crypto.EncryptedConverter;
import com.example.boilerplate.common.entity.BaseEntity;
import com.example.boilerplate.goods.entity.Goods;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Convert(converter = EncryptedConverter.class)
    @Column(length = 255)
    private String recipientPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;

    @Builder
    private Coupon(String code, String name, Long discountAmount, CouponStatus status,
                   OffsetDateTime expiresAt, String recipientPhone, Goods goods) {
        this.code = code;
        this.name = name;
        this.discountAmount = discountAmount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.recipientPhone = recipientPhone;
        this.goods = goods;
    }

    public void update(String name, Long discountAmount, CouponStatus status,
                       OffsetDateTime expiresAt, String recipientPhone) {
        this.name = name;
        this.discountAmount = discountAmount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.recipientPhone = recipientPhone;
    }
}
