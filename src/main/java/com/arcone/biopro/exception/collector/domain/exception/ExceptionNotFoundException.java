package com.arcone.biopro.exception.collector.domain.exception;

/**
 * Exception thrown when a requested exception is not found.
 * Used for HTTP 404 responses in REST API.
 */
public class ExceptionNotFoundException extends RuntimeException {

    public ExceptionNotFoundException(String transactionId) {
        super(String.format("Exception with transaction ID '%s' not found", transactionId));
    }

    public ExceptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}