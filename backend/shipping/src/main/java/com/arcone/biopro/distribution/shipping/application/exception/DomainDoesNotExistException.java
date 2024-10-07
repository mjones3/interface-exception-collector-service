package com.arcone.biopro.distribution.shipping.application.exception;

public class DomainDoesNotExistException extends RuntimeException {

    public DomainDoesNotExistException(String key) {
        super(String.format("Domain with ID %s does not exist", key));
    }

}
