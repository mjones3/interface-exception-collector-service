package com.arcone.biopro.distribution.eventbridge.domain.model.vo;

import org.springframework.util.Assert;

public record ShipmentCustomer(String customerCode, String customerType) {
    public ShipmentCustomer {
        Assert.notNull(customerCode, "customerCode must not be null");
    }
}
