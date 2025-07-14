package com.arcone.biopro.distribution.irradiation.domain.irradiation.entity;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Inventory {
    private final UnitNumber unitNumber;
    private final String productCode;
    private final Location location;
    private final String status;
    private final String productDescription;
    private final String productFamily;
    private final String statusReason;
    private final String unsuitableReason;
    private final Boolean expired;


    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public boolean isAtLocation(Location targetLocation) {
        return this.location.equals(targetLocation);
    }

}
