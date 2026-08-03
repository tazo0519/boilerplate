package com.example.testsupport;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 미처리 예외(500) 계약 테스트 전용 엔드포인트.
 *
 * <p>일부러 컴포넌트 스캔 범위({@code com.example.boilerplate}) 밖 패키지에 둔다 —
 * 스캔 안 되므로 테스트가 {@code @Import} 로 명시 등록할 때만 존재한다.
 * (스캔 범위 안에 두면 스캔+@Import 이중 등록으로 Ambiguous mapping 이 난다)
 */
@RestController
public class BoomTestController {

    @GetMapping("/test-only/boom")
    public String boom() {
        throw new IllegalStateException("boom");
    }
}
