package com.arcone.biopro.distribution.order.application.exception;

public class QueryDidNotFindAnyResultsException extends RuntimeException {

    public QueryDidNotFindAnyResultsException(String message) {
        super(message);
    }

    public QueryDidNotFindAnyResultsException() {
        this("Query did not find any results.");
    }

}
