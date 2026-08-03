package com.example.boilerplate.common.mask;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code @Masked} 직렬화 회귀 테스트 — Jackson 3(tools.jackson)의 createContextual 경로가
 * 필드별 MaskType 을 올바르게 적용하는지 고정한다.
 */
class MaskedSerializerTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    static class Dto {
        @Masked(MaskType.PHONE)
        public String phone = "010-1234-5678";

        @Masked(MaskType.RRN)
        public String rrn = "900101-1234567";

        @Masked(MaskType.EMAIL)
        public String email = "abcdef@example.com";

        public String plain = "그대로";
    }

    @Test
    @DisplayName("@Masked 필드는 타입별 규칙으로 가려지고 일반 필드는 그대로다")
    void maskedFieldsAreMaskedByTypeAndPlainFieldsUntouched() {
        String json = mapper.writeValueAsString(new Dto());

        assertThat(json).contains("\"010-****-5678\"");
        assertThat(json).contains("\"900101-1******\"");
        assertThat(json).contains("\"a***@example.com\"");
        assertThat(json).contains("\"그대로\"");
        // 원문이 어디에도 남지 않아야 한다
        assertThat(json).doesNotContain("010-1234-5678").doesNotContain("1234567").doesNotContain("abcdef@");
    }

    @Test
    @DisplayName("null 값은 null 로 직렬화된다")
    void nullValueIsSerializedAsNull() {
        Dto dto = new Dto();
        dto.phone = null;
        assertThat(mapper.writeValueAsString(dto)).contains("\"phone\":null");
    }
}
