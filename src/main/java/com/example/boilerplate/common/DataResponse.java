package com.example.boilerplate.common;

import lombok.Getter;

import java.util.List;

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
        if (size != null) {
            return size;
        }
        // items 미설정(no-arg 생성/역직렬화) 시에도 NPE 없이 0 을 반환
        return items != null ? items.size() : 0;
    }
}
