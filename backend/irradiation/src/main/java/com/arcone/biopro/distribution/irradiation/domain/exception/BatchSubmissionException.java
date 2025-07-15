package com.arcone.biopro.distribution.irradiation.domain.exception;

/**
 * Exception thrown when batch submission fails due to business rule violations.
 */
public class BatchSubmissionException extends RuntimeException {

    public BatchSubmissionException(String message) {
        super(message);
    }

    public BatchSubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}