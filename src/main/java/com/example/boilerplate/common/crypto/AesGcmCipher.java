package com.example.boilerplate.common.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

/**
 * 저장 포맷: {@code {version}:{Base64(IV+ciphertext)}} (예: "v1:xxxx...").
 *
 * <p>버전 프리픽스인 이유: 키 로테이션 시 구/신 키 암호문이 DB 에 섞이는데, 마커가
 * 없으면 "모든 키로 복호화 시도 후 GCM 실패를 오라클로 쓰는" 방법밖에 없다. 프리픽스가
 * 있으면 복호화 시 맞는 키를 바로 고를 수 있어 재암호화 없는 로테이션이 가능하다
 * (Vault Transit 의 "vault:v1:..." 관행과 동일 원리). 데이터가 쌓인 뒤 포맷을 바꾸면
 * 전 행 마이그레이션이 필요하므로 템플릿 단계에 확정한다.
 *
 * <p>버전 문자열은 GCM AAD 로도 바인딩한다 — 프리픽스만 바꿔치기하면(v1:→v2:) 다른
 * 키 선택을 유도할 수 있는데, AAD 불일치로 인증이 실패해 조작이 감지된다.
 *
 * <p>@Component 가 아닌 이유: 빈 등록은 CryptoAutoConfiguration 이 조건부로 담당한다.
 * 스캔으로 등록하면 암호화를 안 쓰는 파생 서비스도 키 설정 없이는 부팅이 실패한다.
 */
public final class AesGcmCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final Pattern VERSION_PATTERN = Pattern.compile("v\\d+");

    private final KeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmCipher(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        String version = keyProvider.getActiveVersion();
        if (version == null || !VERSION_PATTERN.matcher(version).matches()) {
            // 형식을 강제하지 않으면 ":" 가 든 버전 문자열이 복호화 파싱을 깨뜨린다
            throw new IllegalStateException(
                    "키 버전 형식이 올바르지 않습니다(요구: v+숫자, 예: v1): " + version);
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getActiveKey(), new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(version.getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return version + ":" + Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-GCM 암호화에 실패했습니다.", e);
        }
    }

    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        int separator = stored.indexOf(':');
        String version = separator > 0 ? stored.substring(0, separator) : "";
        if (!VERSION_PATTERN.matcher(version).matches()) {
            // 프리픽스 없는 구포맷을 조용히 폴백하지 않는다 — "버전 불명" 데이터를 영구
            // 허용하면 프리픽스의 존재 의의(로테이션 시 키 식별)가 사라진다.
            throw new IllegalStateException(
                    "암호문에 키 버전 프리픽스(v1: 등)가 없습니다. 프리픽스 도입 이전"
                            + " 데이터라면 구버전 코드로 복호화한 뒤 재암호화해 마이그레이션하세요.");
        }
        byte[] combined;
        try {
            combined = Base64.getDecoder().decode(stored.substring(separator + 1));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("암호문이 올바른 Base64 형식이 아닙니다.", e);
        }
        if (combined.length <= IV_LENGTH) {
            throw new IllegalStateException("암호문 길이가 비정상입니다.");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyProvider.getKey(version), new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(version.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-GCM 복호화에 실패했습니다. (키 불일치 또는 무결성 손상 가능)", e);
        }
    }
}
