package com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors;

public class DeviceValidationFailureException extends RuntimeException {
    public DeviceValidationFailureException(String message) {
        super(message);
    }
}
