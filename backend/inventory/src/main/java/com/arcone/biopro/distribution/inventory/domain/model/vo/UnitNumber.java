package com.arcone.biopro.distribution.inventory.domain.model.vo;

import org.springframework.util.Assert;

public record UnitNumber(String value) {

    public UnitNumber {
        Assert.hasText(value, "Unit number must not be null or empty");
        if (!isValidFormat(value)){
            throw new IllegalArgumentException("Invalid Unit Number Format!");
        }
    }

    private boolean isValidFormat(String unitNumber) {
        return unitNumber.matches("^[A-Z][0-9]{12}+");
    }

}
