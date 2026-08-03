package com.example.boilerplate.common.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 순수 단위 테스트 — Spring 컨텍스트/DB/Docker 불필요. 단위 레인(`./gradlew test`)에서 실행된다.
 * AES-256-GCM 왕복(암호화→복호화)과 IV 난수화 속성을 검증한다.
 */
class AesGcmCipherTest {

    // 32바이트(AES-256) 더미 키 — 테스트 전용. Base64("LocalOnlyDoNotUseInProd!12345678")
    private static final String KEY_BASE64 = "TG9jYWxPbmx5RG9Ob3RVc2VJblByb2QhMTIzNDU2Nzg=";

    private final AesGcmCipher cipher = newCipher();

    private static AesGcmCipher newCipher() {
        return new AesGcmCipher(new EnvKeyProvider(new EncryptionProperties(KEY_BASE64)));
    }

    @Test
    @DisplayName("암호화 후 복호화하면 원문이 복원된다")
    void encryptThenDecryptRestoresPlaintext() {
        String plaintext = "010-1234-5678";

        String encrypted = cipher.encrypt(plaintext);

        assertThat(encrypted).isNotNull().isNotEqualTo(plaintext);
        assertThat(cipher.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("같은 평문도 매번 다른 암호문이 된다")
    void samePlaintextProducesDifferentCiphertextEachTime() {
        // IV 난수화로 동일 평문이라도 암호문이 달라야 한다
        String plaintext = "동일 평문";

        assertThat(cipher.encrypt(plaintext)).isNotEqualTo(cipher.encrypt(plaintext));
    }

    @Test
    @DisplayName("null 은 그대로 null 로 처리된다")
    void nullPassesThroughAsNull() {
        assertThat(cipher.encrypt(null)).isNull();
        assertThat(cipher.decrypt(null)).isNull();
    }
}
