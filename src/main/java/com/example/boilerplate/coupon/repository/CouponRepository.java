package com.example.boilerplate.coupon.repository;

import com.example.boilerplate.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    boolean existsByCode(String code);
}
