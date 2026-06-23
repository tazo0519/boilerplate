package com.example.boilerplate.coupon.entity;

import com.example.boilerplate.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column
    private String recipientPhone;

    @Builder
    private Coupon(String code, String name, Long discountAmount, CouponStatus status,
                   OffsetDateTime expiresAt, String recipientPhone) {
        this.code = code;
        this.name = name;
        this.discountAmount = discountAmount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.recipientPhone = recipientPhone;
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
