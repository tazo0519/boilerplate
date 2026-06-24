package com.example.boilerplate.common.crypto;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class EnvKeyProvider implements KeyProvider {

    private static final int AES_256_KEY_BYTES = 32;

    private final SecretKey activeKey;

    public EnvKeyProvider(EncryptionProperties properties) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(properties.getKeyBase64());
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
