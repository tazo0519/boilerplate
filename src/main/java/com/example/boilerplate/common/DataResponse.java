package com.example.boilerplate.common;

import lombok.Getter;

import java.util.List;

@Getter
public class DataResponse<T> {

    private List<T> items;
    private Integer page;
    private Integer size;
    private Long totalSize;

    // 생성은 ResponseBuilder/BaseController 를 통해서만 한다(package-private).
    // items 없는 no-arg 생성자는 invalid 객체를 만들 수 있어 제공하지 않는다.
    DataResponse(List<T> items) { // 페이징 하지 않고 전체 응답용
        this.items = items;
    }

    DataResponse(List<T> items, Integer page, Integer size, Long totalSize) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalSize = totalSize;
    }

    public Integer getSize() {
        if (size != null) {
            return size;
        }
        // 방어: items 가 null 이어도 NPE 없이 0 을 반환
        return items != null ? items.size() : 0;
    }
}
