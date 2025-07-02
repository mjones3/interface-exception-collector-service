package com.arcone.biopro.distribution.irradiation.domain.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException() {
        super("Inventory does not exist");
    }

    public InventoryNotFoundException(String message) {
        super(message);
    }
}
