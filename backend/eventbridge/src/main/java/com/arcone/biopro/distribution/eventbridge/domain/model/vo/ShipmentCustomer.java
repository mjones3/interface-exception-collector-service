package com.arcone.biopro.distribution.eventbridge.domain.model.vo;

import org.springframework.util.Assert;

public record ShipmentCustomer(String customerCode, String customerType , String customerName , String departmentCode) {
    public ShipmentCustomer {
        Assert.notNull(customerCode, "customerCode must not be null");
        Assert.notNull(customerName, "customerName must not be null");
    }
}
