package com.arcone.biopro.distribution.order.infrastructure.exception;

public class InventoryServiceNotAvailableException extends RuntimeException {
    public InventoryServiceNotAvailableException(String message) {
        super(message);
    }
}
