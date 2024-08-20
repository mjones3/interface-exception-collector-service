package com.arcone.biopro.distribution.inventory.domain.exception;

public class InventoryAlreadyExistsException extends RuntimeException {

    public InventoryAlreadyExistsException() {
        super("Inventory already exists");
    }
}
