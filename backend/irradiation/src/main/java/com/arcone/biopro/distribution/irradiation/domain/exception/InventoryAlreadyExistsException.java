package com.arcone.biopro.distribution.irradiation.domain.exception;

public class InventoryAlreadyExistsException extends RuntimeException {

    public InventoryAlreadyExistsException() {
        super("Inventory already exists");
    }
}
