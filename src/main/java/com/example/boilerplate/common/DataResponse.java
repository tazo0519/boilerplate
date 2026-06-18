package com.example.boilerplate.common;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class DataResponse<T> {

    private List<T> items;
    private Integer page;
    private Integer size;
    private Long totalSize;

    public DataResponse() {

    }

    public DataResponse(List<T> items) { // 페이징 하지 않고 전체 응답용
        this.items = items;
    }

    public DataResponse(List<T> items, Integer page, Integer size, Long totalSize) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalSize = totalSize;
    }

    public Integer getSize() {
        return Objects.nonNull(size) ? size : items.size();
    }
}
