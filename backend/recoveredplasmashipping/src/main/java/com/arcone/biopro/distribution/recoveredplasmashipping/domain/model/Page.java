package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class Page<T> implements Validatable {

    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalRecords;
    private final QuerySort querySort;

    public Page(List<T> content, int pageNumber, int pageSize, long totalRecords, QuerySort querySort) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalRecords = totalRecords;
        this.querySort = querySort;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (this.content == null) {
            throw new IllegalArgumentException("Content must not be null");
        }
        if (this.pageNumber < 0) {
            throw new IllegalArgumentException("Page number must not be less than 0");
        }
        if (this.pageSize < 0) {
            throw new IllegalArgumentException("Page size must not be less than 0");
        }
        if (this.totalRecords < 0) {
            throw new IllegalArgumentException("Total records must not be less than 0");
        }
    }

}
