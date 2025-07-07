package com.arcone.biopro.distribution.irradiation.domain.irradiation.entity;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;

public class Inventory {
    private final UnitNumber unitNumber;
    private final String productCode;
    private final Location location;
    private final String status;

    public Inventory(UnitNumber unitNumber, String productCode, Location location, String status) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.location = location;
        this.status = status;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public boolean isAtLocation(Location targetLocation) {
        return this.location.equals(targetLocation);
    }

    public UnitNumber getUnitNumber() {
        return unitNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public Location getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }
}