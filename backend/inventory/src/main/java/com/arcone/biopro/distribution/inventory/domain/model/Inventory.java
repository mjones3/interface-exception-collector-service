package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.Location;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class Inventory {

    private UUID id;

    private UnitNumber unitNumber;

    private ProductCode productCode;

    private InventoryStatus inventoryStatus;

    private String expirationDate;

    private Location location;

    private ZonedDateTime createDate;

    private ZonedDateTime modificationDate;

    Inventory(
        UnitNumber unitNumber,
        ProductCode productCode,
        String expirationDate,
        Location location) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.expirationDate = expirationDate;
        this.location = location;
        this.inventoryStatus = InventoryStatus.AVAILABLE;
        this.id = UUID.randomUUID();
    }
}
