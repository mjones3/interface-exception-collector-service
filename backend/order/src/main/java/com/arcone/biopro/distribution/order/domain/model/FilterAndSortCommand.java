package com.arcone.biopro.distribution.order.domain.model;

public interface FilterAndSortCommand {

    QuerySort getQuerySort();
    Integer getPageSize();
    Integer getPageNumber();

}
