package com.example.boilerplate.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.boilerplate.TestcontainersConfiguration;
import com.example.testsupport.BoomTestController;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; // Boot 4: 모듈별 테스트 패키지로 이동
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 에러 응답 계약 회귀 테스트 — "어디서 실패하든 동일 래퍼({@code errors.code/message/traceId/timestamp})"를
 * 고정한다. 각 케이스는 적대적 검증(부팅 프로브)에서 실측된 동작이며, 이 테스트가 깨지면
 * Boot/Framework 업그레이드 등으로 에러 계약이 회귀한 것이다.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, BoomTestController.class}) // Boom 은 스캔 밖 패키지 — @Import 로만 등록
class ErrorContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 존재하지_않는_경로는_404_래퍼로_응답한다() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code").value("COMMON_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.traceId").exists())
                .andExpect(jsonPath("$.content").doesNotExist())
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    void 잘못된_JSON_body_는_400_래퍼로_응답한다() throws Exception {
        mockMvc.perform(post("/coupons").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_BAD_REQUEST"))
                .andExpect(jsonPath("$.errors.traceId").exists());
    }

    @Test
    void 허용되지_않은_메서드는_405_래퍼와_Allow_헤더로_응답한다() throws Exception {
        mockMvc.perform(delete("/goods"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errors.code").value("COMMON_METHOD_NOT_ALLOWED"))
                .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void 협상_실패_406_도_빈_body_가_아니라_JSON_래퍼로_응답한다() throws Exception {
        // 적대적 검증 F1: Content-Type 프리셋이 없으면 렌더러가 재예외를 던져 빈 body 로 붕괴했다.
        mockMvc.perform(get("/coupons").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.code").value("COMMON_NOT_ACCEPTABLE"));
    }

    @Test
    void 검증_실패는_fieldErrors_를_포함한_400_래퍼로_응답한다() throws Exception {
        mockMvc.perform(post("/coupons").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.errors.fieldErrors").isArray())
                .andExpect(jsonPath("$.errors.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.errors.traceId").exists());
    }

    @Test
    void 도메인_예외는_도메인_코드와_해당_상태코드로_응답한다() throws Exception {
        mockMvc.perform(get("/coupons/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code").value("COUPON_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.traceId").exists());
    }

    @Test
    void 존재하지_않는_정렬_필드는_500_이_아니라_400_으로_응답한다() throws Exception {
        mockMvc.perform(get("/coupons").param("sort", "nonexistentField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_BAD_REQUEST"));
    }

    @Test
    void 미처리_예외는_500_래퍼로_응답하며_내부_정보를_노출하지_않는다() throws Exception {
        mockMvc.perform(get("/test-only/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors.code").value("COMMON_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errors.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("boom"))));
    }

    @Test
    void ERROR_디스패치도_래퍼로_수렴한다() throws Exception {
        // 컨테이너 sendError 경로 모사 — ERROR_STATUS_CODE 속성이 있으면 그 상태를 보존한다.
        mockMvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("COMMON_BAD_REQUEST"));
    }

    @Test
    void 에러_컨텍스트_없는_error_직접_호출은_가짜_500_대신_404_다() throws Exception {
        // 적대적 검증 F2: BasicErrorController 는 이 경우 status=999 를 500 으로 보고했다.
        mockMvc.perform(get("/error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code").value("COMMON_NOT_FOUND"));
    }

}
