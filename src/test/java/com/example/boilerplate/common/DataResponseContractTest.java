package com.example.boilerplate.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * 응답 래퍼 계약 회귀 테스트 — 적대적 검증 직렬화 프로브를 테스트로 고정.
 * (동결 코어의 와이어 계약: "items 는 항상 배열", null 메타필드 생략, content/errors 상호 배타)
 */
class DataResponseContractTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    @DisplayName("페이징 없는 목록은 page/totalSize 필드를 생략한다")
    void unpagedListOmitsPageAndTotalSizeFields() {
        String json = mapper.writeValueAsString(ResponseBuilder.build(List.of("a", "b")));
        assertThat(json).isEqualTo("{\"content\":{\"items\":[\"a\",\"b\"],\"size\":2}}");
    }

    @Test
    @DisplayName("페이징 목록은 메타필드 4개를 모두 포함한다")
    void pagedListIncludesAllMetaFields() {
        String json = mapper.writeValueAsString(ResponseBuilder.build(List.of("a"), 0, 20, 93L));
        assertThat(json).contains("\"page\":0").contains("\"size\":20").contains("\"totalSize\":93");
    }

    @Test
    @DisplayName("null 리스트가 유입돼도 items 는 항상 배열이다")
    void itemsIsAlwaysArrayEvenWhenNullListGiven() {
        List<String> nullList = null;
        String json = mapper.writeValueAsString(ResponseBuilder.build(nullList));
        assertThat(json).contains("\"items\":[]").contains("\"size\":0");
    }

    @Test
    @DisplayName("생성 후 원본 리스트를 변조해도 응답에 주입되지 않는다")
    void mutatingSourceListAfterBuildDoesNotAffectResponse() {
        List<String> mutable = new ArrayList<>(List.of("x"));
        Response<DataResponse<String>> response = ResponseBuilder.build(mutable);
        mutable.add("INJECTED-AFTER-BUILD");

        assertThat(mapper.writeValueAsString(response)).doesNotContain("INJECTED");
    }

    @Test
    @DisplayName("단건 응답은 errors 를, 에러 응답은 content 를 생략한다")
    void contentAndErrorsAreMutuallyExclusive() {
        assertThat(mapper.writeValueAsString(ResponseBuilder.build("hello")))
                .isEqualTo("{\"content\":\"hello\"}");

        ErrorResponse error = ErrorResponse.builder().code("COMMON_BAD_REQUEST").message("m").build();
        String errorJson = mapper.writeValueAsString(ResponseBuilder.build(error));
        assertThat(errorJson).startsWith("{\"errors\":").doesNotContain("\"content\"");
    }
}
