package com.arcone.biopro.distribution.irradiation.domain.irradiation.entity;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Inventory {
    private final UnitNumber unitNumber;
    private final String productCode;
    private final Location location;
    private final String status;
    private final String productDescription;
    private final String productFamily;
    private final LocalDateTime expirationDate;
    private final String statusReason;
    private final String unsuitableReason;
    private final Boolean expired;
    private final Boolean isImported;
    private final Boolean isBeingIrradiated;
    private final List<InventoryQuarantine> quarantines;


    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public boolean isEligibleForIrradiation() {
        // Exclude converted, modified, in transit, shipped products
        return isAvailable() &&
               !"CONVERTED".equals(status) &&
               !"MODIFIED".equals(status) &&
               !"IN_TRANSIT".equals(status) &&
               !"SHIPPED".equals(status);
    }

    public boolean isAtLocation(Location targetLocation) {
        return this.location.equals(targetLocation);
    }

}
