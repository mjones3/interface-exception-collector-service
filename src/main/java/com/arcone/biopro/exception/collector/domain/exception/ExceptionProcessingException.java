package com.arcone.biopro.exception.collector.domain.exception;

/**
 * Exception thrown when there are errors processing exception events.
 * Used for business logic errors in exception processing pipeline.
 */
public class ExceptionProcessingException extends RuntimeException {

    public ExceptionProcessingException(String message) {
        super(message);
    }

    public ExceptionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}