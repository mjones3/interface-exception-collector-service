package com.arcone.biopro.distribution.order.application.exception;

public class QueryDomainNotFoundException extends RuntimeException {

    public QueryDomainNotFoundException() {
        super("Search Criteria Not Found");
    }
}
