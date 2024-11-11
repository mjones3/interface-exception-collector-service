package com.arcone.biopro.distribution.eventbridge.domain.model.vo;

import org.springframework.util.Assert;

public record ShipmentService(String code, Integer quantity) {
    public ShipmentService {
        Assert.notNull(code, "code must not be null");
        Assert.notNull(quantity, "quantity must not be null");
        Assert.isTrue(quantity > 0, "quantity must be greater than zero");
    }
}
