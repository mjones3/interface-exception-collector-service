package com.arcone.biopro.distribution.inventory.domain.exception;

public class InvalidUpdateProductStatusException extends RuntimeException {

    public InvalidUpdateProductStatusException() {
        super("Inventory no longer AVAILABLE or UNLABELED");
    }
}
