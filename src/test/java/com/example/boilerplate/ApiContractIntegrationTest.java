package com.example.boilerplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.testsupport.BoomTestController;
import jakarta.servlet.RequestDispatcher;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; // Boot 4: 모듈별 테스트 패키지로 이동
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * HTTP API 와이어 계약 회귀 테스트.
 *
 * <p>① 에러 계약 — "어디서 실패하든 동일 래퍼({@code errors.code/message/traceId/timestamp})".
 * 각 케이스는 적대적 검증(부팅 프로브)에서 실측된 동작이다.
 * ② traceId 상관관계 — 응답 헤더와 에러 본문의 traceId 가 같은 값(로그 추적 루프의 핵심).
 * ③ 페이징 성공 경로 — respond(Page) 매핑 계층.
 * ②③은 뮤테이션 검증에서 "생존"으로 실증된 사각지대를 메운 것이다.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, BoomTestController.class}) // Boom 은 스캔 밖 패키지 — @Import 로만 등록
class ApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== 상관관계·성공 경로 계약 ====================

    @Test
    @DisplayName("응답 헤더와 에러 본문의 traceId 는 같은 값이다")
    void traceIdInHeaderAndErrorBodyAreEqual() throws Exception {
        // 뮤테이션 검증 S1: '존재' 단언만으로는 본문 traceId 를 상수로 바꿔도 통과했다 — 동등성을 고정.
        MvcResult result = mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound())
                .andReturn();

        String headerTraceId = result.getResponse().getHeader("X-Trace-Id");
        String bodyTraceId = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.errors.traceId");

        assertThat(headerTraceId).isNotBlank();
        assertThat(bodyTraceId).isEqualTo(headerTraceId);
    }

    @Test
    @DisplayName("페이징 요청은 page/size 메타데이터를 정확히 반영한다")
    void pagingRequestReflectsPageAndSizeMetadata() throws Exception {
        // 뮤테이션 검증 S2: respond(Page) 에서 page/size 를 맞바꿔도 전 스위트가 통과했다 — 매핑을 고정.
        mockMvc.perform(get("/samples").param("page", "2").param("size", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.items").isArray())
                .andExpect(jsonPath("$.content.page").value(2))
                .andExpect(jsonPath("$.content.size").value(7))
                .andExpect(jsonPath("$.content.totalSize").value(0))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    // ==================== 에러 계약 ====================

    @Test
    @DisplayName("존재하지 않는 경로는 404 래퍼로 응답한다")
    void unknownPathRespondsWith404Wrapper() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code").value("COMMON_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.traceId").exists())
                .andExpect(jsonPath("$.content").doesNotExist())
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    @DisplayName("잘못된 JSON body 는 400 래퍼로 응답한다")
    void malformedJsonBodyRespondsWith400Wrapper() throws Exception {
        mockMvc.perform(post("/samples").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_BAD_REQUEST"))
                .andExpect(jsonPath("$.errors.traceId").exists());
    }

    @Test
    @DisplayName("허용되지 않은 메서드는 405 래퍼와 Allow 헤더로 응답한다")
    void methodNotAllowedRespondsWith405WrapperAndAllowHeader() throws Exception {
        mockMvc.perform(delete("/samples"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errors.code").value("COMMON_METHOD_NOT_ALLOWED"))
                .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    @DisplayName("협상 실패(406)도 빈 body 가 아니라 JSON 래퍼로 응답한다")
    void notAcceptableRespondsWithJsonWrapperInsteadOfEmptyBody() throws Exception {
        // 적대적 검증 F1: Content-Type 프리셋이 없으면 렌더러가 재예외를 던져 빈 body 로 붕괴했다.
        mockMvc.perform(get("/samples").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.code").value("COMMON_NOT_ACCEPTABLE"));
    }

    @Test
    @DisplayName("검증 실패는 fieldErrors 를 포함한 400 래퍼로 응답한다")
    void validationFailureRespondsWith400WrapperIncludingFieldErrors() throws Exception {
        mockMvc.perform(post("/samples").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.errors.fieldErrors").isArray())
                .andExpect(jsonPath("$.errors.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.errors.traceId").exists());
    }

    @Test
    @DisplayName("도메인 예외는 도메인 코드와 해당 상태코드로 응답한다")
    void domainExceptionRespondsWithDomainCodeAndStatus() throws Exception {
        mockMvc.perform(get("/samples/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code").value("SAMPLE_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.traceId").exists());
    }

    @Test
    @DisplayName("존재하지 않는 정렬 필드는 500 이 아니라 400 으로 응답한다")
    void unknownSortFieldRespondsWith400InsteadOf500() throws Exception {
        mockMvc.perform(get("/samples").param("sort", "nonexistentField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_BAD_REQUEST"));
    }

    @Test
    @DisplayName("미처리 예외는 500 래퍼로 응답하며 내부 정보를 노출하지 않는다")
    void unhandledExceptionRespondsWith500WrapperWithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/test-only/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors.code").value("COMMON_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errors.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("boom"))));
    }

    @Test
    @DisplayName("ERROR 디스패치도 래퍼로 수렴한다")
    void errorDispatchConvergesToWrapper() throws Exception {
        // 컨테이너 sendError 경로 모사 — ERROR_STATUS_CODE 속성이 있으면 그 상태를 보존한다.
        mockMvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_BAD_REQUEST"));
    }

    @Test
    @DisplayName("에러 컨텍스트 없는 /error 직접 호출은 가짜 500 대신 404 다")
    void directErrorCallWithoutContextRespondsWith404InsteadOfFake500() throws Exception {
        // 적대적 검증 F2: BasicErrorController 는 이 경우 status=999 를 500 으로 보고했다.
        mockMvc.perform(get("/error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code").value("COMMON_NOT_FOUND"));
    }

}
