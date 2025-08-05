package com.arcone.biopro.distribution.receiving.infrastructure.controller.error;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException(String message) {
        super(message);
    }

}
