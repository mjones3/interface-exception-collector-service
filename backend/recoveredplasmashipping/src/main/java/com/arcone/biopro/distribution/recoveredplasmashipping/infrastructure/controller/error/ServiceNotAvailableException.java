package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.error;

public class ServiceNotAvailableException extends RuntimeException {

    public ServiceNotAvailableException(String message) {
        super(message);
    }

}
