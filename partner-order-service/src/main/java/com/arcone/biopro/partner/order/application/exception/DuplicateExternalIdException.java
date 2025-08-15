package com.arcone.biopro.partner.order.application.exception;

/**
 * Exception thrown when attempting to create an order with a duplicate external
 * ID.
 */
public class DuplicateExternalIdException extends RuntimeException {

    private final String externalId;

    public DuplicateExternalIdException(String externalId) {
        super("Order with external ID '" + externalId + "' already exists");
        this.externalId = externalId;
    }

    public String getExternalId() {
        return externalId;
    }
}