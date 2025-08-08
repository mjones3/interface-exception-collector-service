package com.arcone.biopro.exception.collector.domain.exception;

/**
 * Exception thrown when retry is attempted on a non-retryable exception.
 * Used for HTTP 409 responses in retry operations.
 */
public class RetryNotAllowedException extends RuntimeException {

    public RetryNotAllowedException(String transactionId) {
        super(String.format("Exception with transaction ID '%s' is not retryable", transactionId));
    }

    public RetryNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}