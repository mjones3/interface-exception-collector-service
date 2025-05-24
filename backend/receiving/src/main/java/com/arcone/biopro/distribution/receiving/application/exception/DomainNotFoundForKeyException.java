package com.arcone.biopro.distribution.receiving.application.exception;

public class DomainNotFoundForKeyException extends RuntimeException {

    public DomainNotFoundForKeyException(String key) {
        super(String.format("Domain not found for key %s", key));
    }

}
