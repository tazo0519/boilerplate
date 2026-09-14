package com.example.boilerplate.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * @Component 가 아닌 이유: 빈 등록은 CryptoAutoConfiguration 이 조건부로 담당한다.
 * 엔티티가 이 컨버터를 참조하는데 crypto 모듈이 꺼져 있으면(키 미설정) Hibernate 의
 * 기본 생성자 인스턴스화가 실패해 부팅이 중단된다 — 키 없이 조용히 평문 저장되는
 * 것보다 낫다(fail-fast).
 */
@Converter
public final class EncryptedConverter implements AttributeConverter<String, String> {

    private final AesGcmCipher cipher;

    public EncryptedConverter(AesGcmCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return cipher.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return cipher.decrypt(dbData);
    }
}
