package com.example.boilerplate.common.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 순수 단위 테스트 — Spring 컨텍스트/DB/Docker 불필요. 단위 레인(`./gradlew test`)에서 실행된다.
 * AES-256-GCM 왕복, IV 난수화, 그리고 저장 포맷 계약("{version}:{Base64(IV+CT)}")을 검증한다.
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

    @Test
    @DisplayName("암호문은 키 버전 프리픽스(v1:)로 시작한다 — 와이어 계약")
    void ciphertextStartsWithKeyVersionPrefix() {
        assertThat(cipher.encrypt("평문")).startsWith("v1:");
    }

    @Test
    @DisplayName("버전 프리픽스 없는 암호문은 마이그레이션 안내와 함께 거부된다")
    void decryptRejectsCiphertextWithoutVersionPrefix() {
        // 프리픽스 도입 이전 포맷(Base64 만) — 조용한 폴백 없이 fail-fast 해야 한다
        String legacyFormat = cipher.encrypt("평문").substring("v1:".length());

        assertThatThrownBy(() -> cipher.decrypt(legacyFormat))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("프리픽스")
                .hasMessageContaining("마이그레이션");
    }

    @Test
    @DisplayName("알 수 없는 키 버전의 암호문은 명확한 예외로 거부된다")
    void decryptRejectsUnknownKeyVersion() {
        String tampered = "v9" + cipher.encrypt("평문").substring("v1".length());

        assertThatThrownBy(() -> cipher.decrypt(tampered))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("알 수 없는 키 버전");
    }

    @Test
    @DisplayName("버전 프리픽스만 바꿔치기하면 AAD 불일치로 복호화가 실패한다")
    void prefixTamperingFailsAuthenticationViaAad() {
        // v1/v2 가 같은 키를 쓰는 provider — 키 차이를 제거해 실패 원인을 AAD 로 격리한다.
        // updateAAD 를 제거하는 뮤테이션 시 이 테스트만 정확히 실패해야 한다.
        SecretKey sharedKey = new SecretKeySpec(new byte[32], "AES");
        AesGcmCipher sameKeyBothVersions = new AesGcmCipher(new KeyProvider() {
            @Override
            public SecretKey getActiveKey() {
                return sharedKey;
            }

            @Override
            public SecretKey getKey(String version) {
                return sharedKey;
            }
        });

        String tampered = "v2" + sameKeyBothVersions.encrypt("평문").substring("v1".length());

        assertThatThrownBy(() -> sameKeyBothVersions.decrypt(tampered))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("복호화에 실패");
    }

    @Test
    @DisplayName("키 로테이션 후에도 구 버전 암호문은 복호화되고 신규 암호화는 새 버전을 쓴다")
    void rotationDecryptsOldCiphertextAndEncryptsWithNewVersion() {
        SecretKey v1Key = new SecretKeySpec(new byte[32], "AES");
        SecretKey v2Key = new SecretKeySpec(newFilledKey((byte) 7), "AES");
        String v1Ciphertext = new AesGcmCipher(() -> v1Key).encrypt("로테이션 이전 데이터");

        // active 를 v2 로 올린 로테이션 지원 provider — v1 키도 계속 안다
        AesGcmCipher rotated = new AesGcmCipher(new KeyProvider() {
            @Override
            public SecretKey getActiveKey() {
                return v2Key;
            }

            @Override
            public String getActiveVersion() {
                return "v2";
            }

            @Override
            public SecretKey getKey(String version) {
                return switch (version) {
                    case "v1" -> v1Key;
                    case "v2" -> v2Key;
                    default -> throw new IllegalStateException("알 수 없는 키 버전: " + version);
                };
            }
        });

        assertThat(rotated.decrypt(v1Ciphertext)).isEqualTo("로테이션 이전 데이터");
        assertThat(rotated.encrypt("로테이션 이후 데이터")).startsWith("v2:");
    }

    private static byte[] newFilledKey(byte value) {
        byte[] key = new byte[32];
        java.util.Arrays.fill(key, value);
        return key;
    }
}
