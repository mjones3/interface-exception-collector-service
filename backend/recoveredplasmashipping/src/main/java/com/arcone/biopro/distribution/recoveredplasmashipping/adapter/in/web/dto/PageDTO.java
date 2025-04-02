package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Builder;

import java.util.List;

@Builder
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
