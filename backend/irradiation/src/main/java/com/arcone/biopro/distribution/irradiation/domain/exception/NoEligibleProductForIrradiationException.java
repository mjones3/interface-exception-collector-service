package com.arcone.biopro.distribution.irradiation.domain.exception;

public class NoEligibleProductForIrradiationException extends RuntimeException {

    public NoEligibleProductForIrradiationException() {
        super("No products eligible for irradiation");
    }

    public NoEligibleProductForIrradiationException(String message) {
        super(message);
    }
}
