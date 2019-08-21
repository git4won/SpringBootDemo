package com.example.jerseydemo.rest;

import java.util.List;

public class PageableResponse<T> extends ListResponse<T> {

    private int totalPages;
    private long totalElements;

    public PageableResponse() {
        super();
    }

    public PageableResponse(int totalPages, long totalElements, List<T> datas) {
        super();
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
