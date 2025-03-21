package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

public interface FilterAndSortCommand {

    QuerySort getQuerySort();
    Integer getPageSize();
    Integer getPageNumber();

}
