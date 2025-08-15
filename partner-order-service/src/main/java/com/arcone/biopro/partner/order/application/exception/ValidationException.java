package com.arcone.biopro.partner.order.application.exception;

import java.util.List;

/**
 * Exception thrown when order validation fails.
 */
public class ValidationException extends RuntimeException {

    private final List<String> validationErrors;

    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public ValidationException(List<String> validationErrors) {
        super("Validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}