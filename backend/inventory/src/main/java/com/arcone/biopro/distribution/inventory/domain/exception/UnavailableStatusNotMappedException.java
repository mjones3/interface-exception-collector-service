package com.arcone.biopro.distribution.inventory.domain.exception;

public class UnavailableStatusNotMappedException extends RuntimeException {

    public UnavailableStatusNotMappedException() {
        super("Unavailable inventory status was not mapped!");
    }
}
