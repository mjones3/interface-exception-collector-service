package com.arcone.biopro.distribution.shipping.infrastructure.service.errors;

public class InventoryServiceNotAvailableException extends RuntimeException {
    public InventoryServiceNotAvailableException(String message) {
        super(message);
    }
}
