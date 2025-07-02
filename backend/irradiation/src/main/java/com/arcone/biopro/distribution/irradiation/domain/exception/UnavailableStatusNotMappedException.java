package com.arcone.biopro.distribution.irradiation.domain.exception;

public class UnavailableStatusNotMappedException extends RuntimeException {

    public UnavailableStatusNotMappedException() {
        super("Unavailable irradiation status was not mapped!");
    }
}
