package com.arcone.biopro.distribution.order.application.exception;

public class DomainExistsException extends RuntimeException {

    public DomainExistsException(String key) {
        super(String.format("Domain with ID %s already exists", key));
    }

}
