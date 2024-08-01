package com.arcone.biopro.distribution.inventory.domain.model.vo;

import org.springframework.util.Assert;

public record ProductCode(String value) {

    public ProductCode {
        Assert.hasText(value, "Product code must not be null or empty");
        if (!isValidFormat(value)){
            throw new IllegalArgumentException("Invalid Product Code Format!");
        }
    }
    // ^E\d{4}[A-Z]\d{2}$ -- with 6th digit
    // ^E\d{6}$           -- without 6th digit
    private boolean isValidFormat(String value) {
        return value.matches("^E\\d{6}$");
    }

}
