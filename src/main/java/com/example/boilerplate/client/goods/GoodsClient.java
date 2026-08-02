package com.example.boilerplate.client.goods;

import com.example.boilerplate.client.goods.dto.GoodsApiEnvelope;
import com.example.boilerplate.client.goods.dto.GoodsApiResponse;
import com.example.boilerplate.client.goods.dto.GoodsQuoteApiRequest;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 선언형 HTTP 클라이언트 레퍼런스 — 새 클라이언트를 만들 때 필요한 패턴을 복사한다.
 * 메서드 = 외부 API 계약 하나. 구현은 없고 HTTP Service Group(@ImportHttpServices)이 프록시를 생성한다.
 */
@HttpExchange
public interface GoodsClient {

    /** GET + 경로 변수. */
    @GetExchange("/goods/{goodsId}/price")
    GoodsApiResponse fetchPrice(@PathVariable Long goodsId);

    /**
     * [레퍼런스] GET + 쿼리 파라미터, 엔벨로프 응답.
     * {@code ?keyword=..&page=..} 로 직렬화되고, 응답은 파트너 껍데기째 역직렬화된다.
     */
    @GetExchange("/goods")
    GoodsApiEnvelope<List<GoodsApiResponse>> search(@RequestParam String keyword,
                                                    @RequestParam int page);

    /**
     * [레퍼런스] POST + 요청 body + 커스텀 헤더.
     * 멱등키 헤더는 결제·주문 등 재시도 가능한 쓰기 API 에서 중복 처리를 막는 상용 패턴.
     */
    @PostExchange("/goods/quotes")
    GoodsApiEnvelope<GoodsApiResponse> requestQuote(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                    @RequestBody GoodsQuoteApiRequest request);
}
