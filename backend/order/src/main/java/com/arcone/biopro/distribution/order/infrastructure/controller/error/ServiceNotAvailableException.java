package com.arcone.biopro.distribution.order.infrastructure.controller.error;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException(String message) {
        super(message);
    }

}
