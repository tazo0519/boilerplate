package com.example.boilerplate.common.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.boilerplate.TestcontainersConfiguration;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 커스터마이징 훅 계약 회귀 테스트 — "서비스가 자체 KeyProvider 빈을 정의하면 기본
 * (EnvKeyProvider)이 물러난다"를 검증한다.
 *
 * <p>배경: 이 계약은 CryptoAutoConfiguration 이 일반 @Configuration 이던 시절 빈 등록
 * 순서에 따라 깨졌다(NoUniqueBeanDefinitionException 으로 기동 실패 — 적대적 검증에서
 * 실측). auto-configuration 전환 후에는 사용자 빈이 항상 먼저 등록되어 조건이 결정적이다.
 * 이 테스트가 실패한다면 훅 계약이 다시 깨진 것이다.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class KeyProviderOverrideTest {

    @Autowired
    private KeyProvider keyProvider;

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("자체 KeyProvider 빈을 정의하면 기본 EnvKeyProvider 가 물러난다")
    void customKeyProviderBeanBacksOffDefaultEnvKeyProvider() {
        // 물러나지 않으면 이 테스트는 여기까지 오지도 못한다(빈 중복으로 컨텍스트 기동 실패).
        assertThat(context.getBeansOfType(KeyProvider.class)).hasSize(1);
        assertThat(keyProvider).isNotInstanceOf(EnvKeyProvider.class);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class CustomKeyProviderConfig {

        @Bean
        KeyProvider customKeyProvider() {
            // 테스트 더미 키(32바이트 전부 0) — KMS/Vault 구현으로 교체하는 상황을 모사
            return () -> new SecretKeySpec(new byte[32], "AES");
        }
    }
}
