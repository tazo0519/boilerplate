package com.example.boilerplate.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // 페이징 없는 응답에서 page/totalSize null 필드를 생략 — 필드 존재 여부 자체가 "페이징 여부"를 뜻한다
public class DataResponse<T> {

    private List<T> items;
    private Integer page;
    private Integer size;
    private Long totalSize;

    // 생성은 ResponseBuilder/BaseController 를 통해서만 한다(package-private).
    // items 없는 no-arg 생성자는 invalid 객체를 만들 수 있어 제공하지 않는다.
    DataResponse(List<T> items) { // 페이징 하지 않고 전체 응답용
        this.items = normalize(items);
    }

    DataResponse(List<T> items, Integer page, Integer size, Long totalSize) {
        this.items = normalize(items);
        this.page = page;
        this.size = size;
        this.totalSize = totalSize;
    }

    // 상류(서비스)가 실수로 null 리스트를 넘겨도 응답 계약("items 는 항상 배열")을 지킨다.
    private static <T> List<T> normalize(List<T> items) {
        return items != null ? items : List.of();
    }

    public Integer getSize() {
        return size != null ? size : items.size(); // items 는 생성자에서 non-null 보장
    }
}
