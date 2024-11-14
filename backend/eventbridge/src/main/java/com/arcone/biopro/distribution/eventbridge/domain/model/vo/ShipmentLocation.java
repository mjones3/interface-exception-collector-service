package com.arcone.biopro.distribution.eventbridge.domain.model.vo;

import org.springframework.util.Assert;

public record ShipmentLocation(String shipmentLocationCode, String shipmentLocationName) {

    public ShipmentLocation {
        Assert.notNull(shipmentLocationCode, "Location Code must not be null");
        Assert.notNull(shipmentLocationName, "Location Name must not be null");
    }
}
