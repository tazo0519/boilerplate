package com.example.boilerplate.testsupport;

import com.example.boilerplate.common.BaseController;
import com.example.boilerplate.common.DataResponse;
import com.example.boilerplate.common.Response;
import com.example.boilerplate.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 계약 테스트의 표적 엔드포인트이자 "새 컨트롤러 작성 패턴"의 살아있는 예시 —
 * BaseController 상속 + respond(...) 단일 경로, 검증은 @Valid, 에러는 예외 던지기.
 *
 * <p>com.example.boilerplate 스캔 범위 안(test 소스)이라 컴포넌트 스캔만으로 등록된다 —
 * @Import 를 병용하면 이중 등록(Ambiguous mapping)이 나므로 하지 말 것.
 */
@RestController
@RequestMapping("/samples")
@RequiredArgsConstructor
public class SampleController extends BaseController {

    private final SampleRepository repository;

    @PostMapping
    public Response<SampleResponse> create(@Valid @RequestBody SampleCreateRequest request) {
        return respond(SampleResponse.from(repository.save(new SampleEntity(request.name(), request.phone()))));
    }

    @GetMapping("/{id}")
    public Response<SampleResponse> get(@PathVariable Long id) {
        return respond(repository.findById(id).map(SampleResponse::from)
                .orElseThrow(() -> new BusinessException(SampleErrorCode.SAMPLE_NOT_FOUND, "id=" + id)));
    }

    @GetMapping
    public Response<DataResponse<SampleResponse>> list(Pageable pageable) {
        return respond(repository.findAll(pageable).map(SampleResponse::from));
    }

    public record SampleCreateRequest(@NotBlank String name, @NotBlank String phone) {
    }

    public record SampleResponse(Long id, String name, Instant createdAt) {
        static SampleResponse from(SampleEntity entity) {
            return new SampleResponse(entity.getId(), entity.getName(), entity.getCreatedAt());
        }
    }
}
