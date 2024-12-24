package com.arcone.biopro.distribution.inventory.domain.model.vo;

import org.springframework.util.Assert;

public record ProductCode(String value) {

    public ProductCode {
        Assert.hasText(value, "Product code must not be null or empty");
    }
}
