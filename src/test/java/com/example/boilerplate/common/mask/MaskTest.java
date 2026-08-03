package com.example.boilerplate.common.mask;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 마스킹 규칙 단위 테스트 — PII 노출 방지 로직은 회귀에 특히 민감하다. */
class MaskTest {

    @Test
    @DisplayName("주민번호는 생년월일과 성별자리만 남기고 가린다")
    void rrnKeepsBirthDateAndGenderDigitOnly() {
        assertThat(Mask.rrn("900101-1234567")).isEqualTo("900101-1******");
        assertThat(Mask.rrn("9001011234567")).isEqualTo("900101-1******"); // 하이픈 없이도
    }

    @Test
    @DisplayName("전화번호는 가운데 자리를 가린다")
    void phoneMasksMiddleDigits() {
        assertThat(Mask.phone("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(Mask.phone("01012345678")).isEqualTo("010-****-5678");
    }

    @Test
    @DisplayName("이메일은 첫 글자와 도메인만 남긴다")
    void emailKeepsFirstCharAndDomainOnly() {
        assertThat(Mask.email("abcdef@example.com")).isEqualTo("a***@example.com");
    }

    @Test
    @DisplayName("null 과 형식 미달 입력은 안전하게 처리한다")
    void nullAndMalformedInputsAreHandledSafely() {
        assertThat(Mask.rrn(null)).isNull();
        assertThat(Mask.phone(null)).isNull();
        assertThat(Mask.email(null)).isNull();
        assertThat(Mask.rrn("12345")).isEqualTo("12345");     // 자릿수 미달 — 원본 유지
        assertThat(Mask.phone("1234567")).isEqualTo("1234567");
        assertThat(Mask.email("a@x")).isEqualTo("a@x");       // at 위치 1 이하 — 원본 유지
    }
}
