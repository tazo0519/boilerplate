package com.example.boilerplate.common.crypto;

import static org.assertj.core.api.Assertions.assertThat;

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
        EncryptionProperties props = new EncryptionProperties();
        props.setKeyBase64(KEY_BASE64);
        return new AesGcmCipher(new EnvKeyProvider(props));
    }

    @Test
    void 암호화_후_복호화하면_원문이_복원된다() {
        String plaintext = "010-1234-5678";

        String encrypted = cipher.encrypt(plaintext);

        assertThat(encrypted).isNotNull().isNotEqualTo(plaintext);
        assertThat(cipher.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    void 같은_평문도_매번_다른_암호문이_된다() {
        // IV 난수화로 동일 평문이라도 암호문이 달라야 한다
        String plaintext = "동일 평문";

        assertThat(cipher.encrypt(plaintext)).isNotEqualTo(cipher.encrypt(plaintext));
    }

    @Test
    void null_은_그대로_null_로_처리된다() {
        assertThat(cipher.encrypt(null)).isNull();
        assertThat(cipher.decrypt(null)).isNull();
    }
}
