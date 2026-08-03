package com.example.boilerplate.goods.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.boilerplate.TestcontainersConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 실제 PostgreSQL(Testcontainers) 위에서 스키마·시드 데이터가 정상 적재되고
 * 리포지토리 조회가 동작하는지 검증한다. db/schema.sql 의 goods 시드 3건을 확인한다.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    @DisplayName("시드된 상품 3건이 조회된다")
    void seededGoodsAreQueryable() {
        assertThat(goodsRepository.count()).isEqualTo(3);
    }
}
