package com.arcone.biopro.distribution.shipping.infrastructure.controller.error;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException(String message) {
        super(message);
    }

}
