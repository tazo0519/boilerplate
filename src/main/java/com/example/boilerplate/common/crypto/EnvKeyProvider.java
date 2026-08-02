package com.example.boilerplate.common.crypto;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

// 기본 구현 — CryptoConfig 가 @ConditionalOnMissingBean 으로 등록한다.
// 외부에는 KeyProvider 인터페이스만 노출한다(package-private, 구현 교체는 빈 재정의로).
final class EnvKeyProvider implements KeyProvider {

    private static final int AES_256_KEY_BYTES = 32;

    private final SecretKey activeKey;

    EnvKeyProvider(EncryptionProperties properties) {
        String keyBase64 = properties.keyBase64();
        // @ConfigurationProperties 바인더는 미해석 placeholder(${...})를 예외 없이 리터럴로
        // 바인딩하므로, 환경변수 누락을 여기서 명시적으로 감지해 원인을 정확히 알려준다.
        // (이 검증이 없으면 "Base64 형식 오류"로 보고되어 운영자가 원인을 오독한다)
        if (keyBase64 != null && keyBase64.startsWith("${")) {
            throw new IllegalStateException(
                    "암호화 키 환경변수가 주입되지 않았습니다. placeholder 가 미해석 상태입니다: " + keyBase64
                            + " — ENCRYPTION_KEY_BASE64 를 환경변수(또는 ECS taskdef secrets)로 주입하세요.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(keyBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "boilerplate.security.crypto.key-base64 값이 올바른 Base64 형식이 아닙니다.", e);
        }
        if (keyBytes.length != AES_256_KEY_BYTES) {
            throw new IllegalStateException(
                    "암호화 키는 Base64 디코드 시 정확히 32바이트(AES-256)여야 합니다. 현재="
                            + keyBytes.length + " bytes");
        }
        this.activeKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public SecretKey getActiveKey() {
        return activeKey;
    }
}
