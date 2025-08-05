package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.List;

public record PageDTO<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalRecords,
    QuerySortDTO querySort
) {

    @JsonGetter
    public boolean hasPrevious() {
        return pageNumber() > 0;
    }

    @JsonGetter
    public boolean hasNext() {
        return pageNumber() + 1 < totalPages();
    }

    @JsonGetter
    public boolean isFirst() {
        return !hasPrevious();
    }

    @JsonGetter
    public boolean isLast() {
        return !hasNext();
    }

    @JsonGetter
    public int totalPages() {
        return (int) Math.ceil((double) totalRecords / (double) pageSize);
    }

}
