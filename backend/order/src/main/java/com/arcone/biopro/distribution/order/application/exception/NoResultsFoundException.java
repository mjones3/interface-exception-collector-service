package com.arcone.biopro.distribution.order.application.exception;

public class NoResultsFoundException extends RuntimeException {

    public NoResultsFoundException(String message) {
        super(message);
    }

    public NoResultsFoundException() {
        this("No Results Found");
    }

}
