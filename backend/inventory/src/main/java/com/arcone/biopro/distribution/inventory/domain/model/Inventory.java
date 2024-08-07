package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.Location;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
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

    private String productDescription;

    private InventoryStatus inventoryStatus;

    private String expirationDate;

    private String collectionDate;

    private Location location;

    private ProductFamily productFamily;

    private AboRhType abo_rh;

    private ZonedDateTime createDate;

    private ZonedDateTime modificationDate;

    Inventory(
        UnitNumber unitNumber,
        ProductCode productCode,
        String productDescription,
        String expirationDate,
        String collectionDate,
        Location location,
        ProductFamily productFamily,
        AboRhType aboRh) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.productDescription = productDescription;
        this.expirationDate = expirationDate;
        this.collectionDate = collectionDate;
        this.location = location;
        this.productFamily = productFamily;
        this.abo_rh = aboRh;
        this.inventoryStatus = InventoryStatus.AVAILABLE;
        this.id = UUID.randomUUID();
    }
}
