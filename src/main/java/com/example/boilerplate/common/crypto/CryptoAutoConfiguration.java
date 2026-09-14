package com.example.boilerplate.common.crypto;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 암호화 기본 구성 — 일반 @Configuration 이 아니라 <b>auto-configuration</b> 이다.
 *
 * <p>이유: {@code @ConditionalOnMissingBean} 은 평가 시점까지 등록된 빈만 보므로, 일반
 * @Configuration 에서는 사용자 빈과의 등록 순서가 보장되지 않아 "물러남"이 비결정적이다
 * (적대적 검증에서 패키지명 순서로 성패가 갈리는 것을 실측). auto-configuration 은 항상
 * 사용자 구성 이후에 평가되므로 조건이 신뢰 가능하다. 이 클래스는
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} 로
 * 등록되며, 컴포넌트 스캔에서는 자동 제외된다(AutoConfigurationExcludeFilter).
 *
 * <p><b>opt-in 모듈</b>: {@code boilerplate.security.crypto.key-base64} 를 설정한 서비스만
 * crypto 빈이 활성화된다. 미설정이면 모듈 전체가 존재하지 않는 것과 같다 — 암호화를
 * 안 쓰는 파생 서비스가 키 환경변수 없이도 부팅되도록(불필요한 fail-fast 방지) 하기 위함.
 * 반대로 키를 설정했다면 EnvKeyProvider 의 placeholder/형식 검증이 그대로 fail-fast 한다.
 */
@AutoConfiguration
@ConditionalOnProperty("boilerplate.security.crypto.key-base64")
@EnableConfigurationProperties(EncryptionProperties.class)
public class CryptoAutoConfiguration {

    /**
     * 기본 키 공급자(환경변수 기반) — 커스터마이징 훅.
     *
     * <p>KMS/Vault 등으로 교체하려면 이 파일을 수정하지 말고, 서비스에서 자체
     * {@link KeyProvider} 빈을 정의하면 된다(기본이 자동으로 물러난다). 키 로테이션이
     * 필요하면 {@code getActiveVersion()}/{@code getKey(version)} 도 함께 재정의한다:
     * <pre>{@code
     * @Bean
     * KeyProvider kmsKeyProvider(KmsClient kms) { ... }
     * }</pre>
     */
    @Bean
    @ConditionalOnMissingBean(KeyProvider.class)
    KeyProvider envKeyProvider(EncryptionProperties properties) {
        return new EnvKeyProvider(properties);
    }

    @Bean
    AesGcmCipher aesGcmCipher(KeyProvider keyProvider) {
        return new AesGcmCipher(keyProvider);
    }

    // Hibernate 는 SpringBeanContainer 를 통해 이 빈으로 @Convert 컨버터를 해석한다.
    @Bean
    EncryptedConverter encryptedConverter(AesGcmCipher cipher) {
        return new EncryptedConverter(cipher);
    }
}
