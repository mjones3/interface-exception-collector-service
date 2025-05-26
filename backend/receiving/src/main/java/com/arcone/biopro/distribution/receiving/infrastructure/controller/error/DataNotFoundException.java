package com.arcone.biopro.distribution.receiving.infrastructure.controller.error;

public class DataNotFoundException extends RuntimeException {

    public DataNotFoundException(String code) {
        super(String.format("Data for code \"%s\" was not found", code));
    }

}
