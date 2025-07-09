package com.arcone.biopro.distribution.irradiation.domain.exception;

public class ConfigurationNotFoundException extends RuntimeException {
    public ConfigurationNotFoundException() {
        super("Configuration not found");
    }
}
