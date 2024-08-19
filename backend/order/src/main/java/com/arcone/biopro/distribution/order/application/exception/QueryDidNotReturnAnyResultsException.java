package com.arcone.biopro.distribution.order.application.exception;

public class QueryDidNotReturnAnyResultsException extends RuntimeException {

    public QueryDidNotReturnAnyResultsException(String message) {
        super(message);
    }

    public QueryDidNotReturnAnyResultsException() {
        this("The query did not return any results.");
    }

}
