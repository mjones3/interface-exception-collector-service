package com.arcone.biopro.distribution.customer.domain.exception;

public class GenericException extends RuntimeException {

    protected String type = "error";

    public GenericException(String message) {
        super(message);
    }

    public String getType() {
        return type;
    }
}
