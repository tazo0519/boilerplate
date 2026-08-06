package com.example.boilerplate.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.boilerplate.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
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
class SampleEncryptionIntegrationTest {

    private static final String PLAIN_PHONE = "010-1234-5678";

    @Autowired
    private SampleRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("전화번호는 DB 에 암호문으로 저장되고 조회 시 평문으로 복원된다")
    void phoneIsStoredEncryptedAndRestoredToPlaintextOnLoad() {
        SampleEntity saved = repository.save(new SampleEntity("암호화 왕복 테스트", PLAIN_PHONE));
        entityManager.flush();
        entityManager.clear(); // 1차 캐시 제거 — DB 재조회를 강제

        // DB 원본 컬럼: 평문이 아니어야 하고, 저장 포맷(Base64) 이어야 한다
        String stored = jdbcTemplate.queryForObject(
                "SELECT phone FROM samples WHERE id = ?", String.class, saved.getId());
        assertThat(stored).isNotNull().isNotEqualTo(PLAIN_PHONE).doesNotContain("1234");
        byte[] decoded = Base64.getDecoder().decode(stored); // 포맷 검증(디코드 실패 시 예외)
        assertThat(decoded.length).isGreaterThan(12 + 16);   // IV(12) + tag(16) + 암호문

        // 엔티티 재조회: 평문 복원
        SampleEntity reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getPhone()).isEqualTo(PLAIN_PHONE);
    }
}
