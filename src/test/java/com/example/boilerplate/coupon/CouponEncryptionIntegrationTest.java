package com.example.boilerplate.coupon;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.boilerplate.TestcontainersConfiguration;
import com.example.boilerplate.coupon.entity.Coupon;
import com.example.boilerplate.coupon.entity.CouponStatus;
import com.example.boilerplate.coupon.repository.CouponRepository;
import com.example.boilerplate.goods.repository.GoodsRepository;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Base64;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 컬럼 암호화(EncryptedConverter) 왕복 회귀 테스트 — 실제 PostgreSQL 위에서
 * "DB 에는 암호문, 엔티티에는 평문" 계약을 고정한다.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class CouponEncryptionIntegrationTest {

    private static final String PLAIN_PHONE = "010-1234-5678";

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 전화번호는_DB_에_암호문으로_저장되고_조회_시_평문으로_복원된다() {
        Coupon saved = couponRepository.save(Coupon.builder()
                .code("ENC-TEST-001")
                .name("암호화 왕복 테스트")
                .discountAmount(1000L)
                .status(CouponStatus.ACTIVE)
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .recipientPhone(PLAIN_PHONE)
                .goods(goodsRepository.findById(1L).orElseThrow()) // schema.sql 시드
                .build());
        entityManager.flush();
        entityManager.clear(); // 1차 캐시 제거 — DB 재조회를 강제

        // DB 원본 컬럼: 평문이 아니어야 하고, 저장 포맷(Base64) 이어야 한다
        String stored = jdbcTemplate.queryForObject(
                "SELECT recipient_phone FROM coupons WHERE id = ?", String.class, saved.getId());
        assertThat(stored).isNotNull().isNotEqualTo(PLAIN_PHONE).doesNotContain("1234");
        byte[] decoded = Base64.getDecoder().decode(stored); // 포맷 검증(디코드 실패 시 예외)
        assertThat(decoded.length).isGreaterThan(12 + 16);   // IV(12) + tag(16) + 암호문

        // 엔티티 재조회: 평문 복원
        Coupon reloaded = couponRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getRecipientPhone()).isEqualTo(PLAIN_PHONE);
    }
}
