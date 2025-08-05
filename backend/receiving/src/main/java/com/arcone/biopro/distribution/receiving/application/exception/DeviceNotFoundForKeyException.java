package com.arcone.biopro.distribution.receiving.application.exception;

public class DeviceNotFoundForKeyException extends RuntimeException {

    public DeviceNotFoundForKeyException(String key) {
        super(String.format("Device not found for blood center ID %s", key));
    }

}
