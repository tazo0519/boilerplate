package com.example.boilerplate.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * 응답 래퍼 계약 회귀 테스트 — 적대적 검증 직렬화 프로브를 테스트로 고정.
 * (동결 코어의 와이어 계약: "items 는 항상 배열", null 메타필드 생략, content/errors 상호 배타)
 */
class DataResponseContractTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void 페이징_없는_목록은_page_totalSize_필드를_생략한다() {
        String json = mapper.writeValueAsString(ResponseBuilder.build(List.of("a", "b")));
        assertThat(json).isEqualTo("{\"content\":{\"items\":[\"a\",\"b\"],\"size\":2}}");
    }

    @Test
    void 페이징_목록은_메타필드_4개를_모두_포함한다() {
        String json = mapper.writeValueAsString(ResponseBuilder.build(List.of("a"), 0, 20, 93L));
        assertThat(json).contains("\"page\":0").contains("\"size\":20").contains("\"totalSize\":93");
    }

    @Test
    void null_리스트가_유입돼도_items_는_항상_배열이다() {
        List<String> nullList = null;
        String json = mapper.writeValueAsString(ResponseBuilder.build(nullList));
        assertThat(json).contains("\"items\":[]").contains("\"size\":0");
    }

    @Test
    void 생성_후_원본_리스트를_변조해도_응답에_주입되지_않는다() {
        List<String> mutable = new ArrayList<>(List.of("x"));
        Response<DataResponse<String>> response = ResponseBuilder.build(mutable);
        mutable.add("INJECTED-AFTER-BUILD");

        assertThat(mapper.writeValueAsString(response)).doesNotContain("INJECTED");
    }

    @Test
    void 단건_응답은_errors_를_에러_응답은_content_를_생략한다() {
        assertThat(mapper.writeValueAsString(ResponseBuilder.build("hello")))
                .isEqualTo("{\"content\":\"hello\"}");

        ErrorResponse error = ErrorResponse.builder().code("COMMON_BAD_REQUEST").message("m").build();
        String errorJson = mapper.writeValueAsString(ResponseBuilder.build(error));
        assertThat(errorJson).startsWith("{\"errors\":").doesNotContain("\"content\"");
    }
}
