package com.example.boilerplate.coupon.dto;

import com.example.boilerplate.coupon.entity.Coupon;
import com.example.boilerplate.coupon.entity.CouponStatus;
import com.example.boilerplate.goods.entity.Goods;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CouponCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Long discountAmount;

    @NotNull
    private CouponStatus status;

    @NotNull
    private OffsetDateTime expiresAt;

    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 휴대폰 번호 형식이어야 합니다")
    private String recipientPhone;

    @NotNull
    private Long goodsId;

    public Coupon toEntity(Goods goods) {
        return Coupon.builder()
                .code(this.code)
                .name(this.name)
                .discountAmount(this.discountAmount)
                .status(this.status)
                .expiresAt(this.expiresAt)
                .recipientPhone(this.recipientPhone)
                .goods(goods)
                .build();
    }
}
