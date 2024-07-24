package com.arcone.biopro.distribution.shippingservice.infrastructure.service.errors;

public class InventoryServiceNotAvailableException extends RuntimeException {
    public InventoryServiceNotAvailableException(String message) {
        super(message);
    }
}
