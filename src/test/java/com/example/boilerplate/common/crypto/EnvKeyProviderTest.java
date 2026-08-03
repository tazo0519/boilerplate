package com.example.boilerplate.common.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import org.junit.jupiter.api.Test;

/**
 * 키 검증 fail-fast 회귀 테스트 — 특히 "미해석 placeholder 감지"는 적대적 검증에서
 * 잘못된 에러 메시지("Base64 형식 오류")로 오독되던 것을 고친 지점이라 반드시 고정한다.
 */
class EnvKeyProviderTest {

    @Test
    void 환경변수_미주입_placeholder_는_원인을_명시한_예외로_부팅을_중단시킨다() {
        assertThatThrownBy(() -> new EnvKeyProvider(new EncryptionProperties("${ENCRYPTION_KEY_BASE64}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주입되지 않았습니다")
                .hasMessageContaining("ENCRYPTION_KEY_BASE64");
    }

    @Test
    void 잘못된_Base64_는_형식_오류로_중단시킨다() {
        assertThatThrownBy(() -> new EnvKeyProvider(new EncryptionProperties("not-base64!!")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void _32바이트가_아닌_키는_길이_오류로_중단시킨다() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new EnvKeyProvider(new EncryptionProperties(shortKey)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32바이트");
    }

    @Test
    void 올바른_32바이트_키는_AES_SecretKey_를_제공한다() {
        String validKey = Base64.getEncoder().encodeToString(new byte[32]);
        EnvKeyProvider provider = new EnvKeyProvider(new EncryptionProperties(validKey));

        assertThat(provider.getActiveKey()).isNotNull();
        assertThat(provider.getActiveKey().getAlgorithm()).isEqualTo("AES");
    }
}
