package com.example.boilerplate.common.crypto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EncryptionProperties.class)
public class CryptoConfig {

    /**
     * 기본 키 공급자(환경변수 기반) — 커스터마이징 훅.
     *
     * <p>KMS/Vault 등으로 교체하려면 이 파일을 수정하지 말고, 서비스에서 자체
     * {@link KeyProvider} 빈을 정의하면 된다(@ConditionalOnMissingBean 으로 기본이 물러난다):
     * <pre>{@code
     * @Bean
     * KeyProvider kmsKeyProvider(KmsClient kms) { ... }
     * }</pre>
     */
    @Bean
    @ConditionalOnMissingBean(KeyProvider.class)
    public KeyProvider envKeyProvider(EncryptionProperties properties) {
        return new EnvKeyProvider(properties);
    }
}
