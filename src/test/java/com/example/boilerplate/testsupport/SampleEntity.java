package com.example.boilerplate.testsupport;

import com.example.boilerplate.common.crypto.EncryptedConverter;
import com.example.boilerplate.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계약 테스트용 픽스처 엔티티 — src/main 에는 도메인 코드를 두지 않는다(순수 보일러플레이트).
 *
 * <p>동시에 "새 도메인 엔티티 작성 패턴"의 살아있는 예시다:
 * {@link BaseEntity} 상속(id·감사시각 자동), PII 컬럼은 {@code @Convert} 로 컬럼 암호화.
 * 스키마는 test 리소스의 {@code db/migration/V1__test_schema.sql} 이 공급한다.
 */
@Getter
@Entity
@Table(name = "samples")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SampleEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Convert(converter = EncryptedConverter.class)
    @Column(length = 255)
    private String phone;

    public SampleEntity(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}
