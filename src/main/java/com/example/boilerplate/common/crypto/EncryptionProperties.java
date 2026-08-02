package com.example.boilerplate.common.crypto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 암호화 키 설정 — 생성자 바인딩 record 라 바인딩 후 불변이다(런타임 키 변조 불가).
 * crypto 패키지 내부 전용(package-private) — 외부에는 {@link KeyProvider} 만 노출한다.
 */
@Validated
@ConfigurationProperties(prefix = "boilerplate.security.crypto")
record EncryptionProperties(@NotBlank String keyBase64) {
}
