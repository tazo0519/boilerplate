package com.example.boilerplate.common.mask;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** 마스킹 규칙 단위 테스트 — PII 노출 방지 로직은 회귀에 특히 민감하다. */
class MaskTest {

    @Test
    void 주민번호는_생년월일과_성별자리만_남기고_가린다() {
        assertThat(Mask.rrn("900101-1234567")).isEqualTo("900101-1******");
        assertThat(Mask.rrn("9001011234567")).isEqualTo("900101-1******"); // 하이픈 없이도
    }

    @Test
    void 전화번호는_가운데_자리를_가린다() {
        assertThat(Mask.phone("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(Mask.phone("01012345678")).isEqualTo("010-****-5678");
    }

    @Test
    void 이메일은_첫_글자와_도메인만_남긴다() {
        assertThat(Mask.email("abcdef@example.com")).isEqualTo("a***@example.com");
    }

    @Test
    void null_과_형식_미달_입력은_안전하게_처리한다() {
        assertThat(Mask.rrn(null)).isNull();
        assertThat(Mask.phone(null)).isNull();
        assertThat(Mask.email(null)).isNull();
        assertThat(Mask.rrn("12345")).isEqualTo("12345");     // 자릿수 미달 — 원본 유지
        assertThat(Mask.phone("1234567")).isEqualTo("1234567");
        assertThat(Mask.email("a@x")).isEqualTo("a@x");       // at 위치 1 이하 — 원본 유지
    }
}
