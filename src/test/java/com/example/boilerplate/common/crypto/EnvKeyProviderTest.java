package com.example.boilerplate.common.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 키 검증 fail-fast 회귀 테스트 — 특히 "미해석 placeholder 감지"는 적대적 검증에서
 * 잘못된 에러 메시지("Base64 형식 오류")로 오독되던 것을 고친 지점이라 반드시 고정한다.
 */
class EnvKeyProviderTest {

    @Test
    @DisplayName("환경변수 미주입 placeholder 는 원인을 명시한 예외로 부팅을 중단시킨다")
    void unresolvedPlaceholderFailsBootWithExplicitCause() {
        assertThatThrownBy(() -> new EnvKeyProvider(new EncryptionProperties("${ENCRYPTION_KEY_BASE64}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주입되지 않았습니다")
                .hasMessageContaining("ENCRYPTION_KEY_BASE64");
    }

    @Test
    @DisplayName("잘못된 Base64 는 형식 오류로 중단시킨다")
    void invalidBase64FailsWithFormatError() {
        assertThatThrownBy(() -> new EnvKeyProvider(new EncryptionProperties("not-base64!!")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    @DisplayName("32바이트가 아닌 키는 길이 오류로 중단시킨다")
    void keyNotThirtyTwoBytesFailsWithLengthError() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new EnvKeyProvider(new EncryptionProperties(shortKey)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32바이트");
    }

    @Test
    @DisplayName("올바른 32바이트 키는 AES SecretKey 를 제공한다")
    void validThirtyTwoByteKeyProvidesAesSecretKey() {
        String validKey = Base64.getEncoder().encodeToString(new byte[32]);
        EnvKeyProvider provider = new EnvKeyProvider(new EncryptionProperties(validKey));

        assertThat(provider.getActiveKey()).isNotNull();
        assertThat(provider.getActiveKey().getAlgorithm()).isEqualTo("AES");
    }
}
