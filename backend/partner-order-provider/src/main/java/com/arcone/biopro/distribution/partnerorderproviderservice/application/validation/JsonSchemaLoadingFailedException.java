package com.arcone.biopro.distribution.partnerorderproviderservice.application.validation;

public class JsonSchemaLoadingFailedException extends RuntimeException {

    public JsonSchemaLoadingFailedException(String message) {
        super(message);
    }

    public JsonSchemaLoadingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
