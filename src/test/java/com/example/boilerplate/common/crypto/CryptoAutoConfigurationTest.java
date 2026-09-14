package com.example.boilerplate.common.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * crypto 모듈 opt-in 계약 회귀 테스트 — "키를 설정한 서비스만 crypto 빈이 뜬다".
 *
 * <p>이 계약이 깨지면(무조건 활성화로 회귀) 암호화를 안 쓰는 파생 서비스가
 * 키 환경변수 없이는 부팅에 실패한다. 순수 단위 레인 — DB/Docker 불필요.
 */
class CryptoAutoConfigurationTest {

    // 32바이트(AES-256) 더미 키 — 테스트 전용. Base64("LocalOnlyDoNotUseInProd!12345678")
    private static final String KEY_BASE64 = "TG9jYWxPbmx5RG9Ob3RVc2VJblByb2QhMTIzNDU2Nzg=";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CryptoAutoConfiguration.class));

    @Test
    @DisplayName("키 미설정이면 crypto 빈이 하나도 등록되지 않는다 — 암호화 미사용 서비스 부팅 보장")
    void cryptoBeansAreAbsentWhenKeyIsNotConfigured() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(KeyProvider.class);
            assertThat(context).doesNotHaveBean(AesGcmCipher.class);
            assertThat(context).doesNotHaveBean(EncryptedConverter.class);
        });
    }

    @Test
    @DisplayName("키를 설정하면 crypto 빈 전체가 활성화된다")
    void cryptoBeansAreRegisteredWhenKeyIsConfigured() {
        runner.withPropertyValues("boilerplate.security.crypto.key-base64=" + KEY_BASE64)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(KeyProvider.class);
                    assertThat(context).hasSingleBean(AesGcmCipher.class);
                    assertThat(context).hasSingleBean(EncryptedConverter.class);
                });
    }

    @Test
    @DisplayName("키가 미해석 placeholder 면 활성화는 되되 fail-fast 한다 — 조용한 비활성화 금지")
    void unresolvedPlaceholderStillFailsFastInsteadOfSilentlyDisabling() {
        // yaml 에 key-base64: ${ENCRYPTION_KEY_BASE64} 를 선언했는데 환경변수가 없는 상황.
        // 핵심 계약: 조건이 "속성 존재"이므로 미해석 placeholder 를 "미설정"으로 오인해
        // 모듈을 조용히 끄면 안 되고, 반드시 부팅 실패로 드러나야 한다(fail-fast 유지).
        // (실패 지점은 placeholder 해석기 또는 EnvKeyProvider 감지 — 어느 층이든 무방)
        runner.withPropertyValues("boilerplate.security.crypto.key-base64=${ENCRYPTION_KEY_BASE64}")
                .run(context -> assertThat(context).hasFailed());
    }
}
