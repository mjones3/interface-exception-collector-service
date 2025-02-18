package com.arcone.biopro.distribution.inventory.domain.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException() {
        super("Inventory does not exist");
    }
}
