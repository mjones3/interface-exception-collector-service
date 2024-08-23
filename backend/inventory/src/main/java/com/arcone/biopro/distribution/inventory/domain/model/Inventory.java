package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class Inventory {

    private UUID id;

    private UnitNumber unitNumber;

    private ProductCode productCode;

    private String shortDescription;

    private InventoryStatus inventoryStatus;

    private LocalDateTime expirationDate;

    private String collectionDate;

    private String location;

    private ProductFamily productFamily;

    private AboRhType aboRh;

    private ZonedDateTime createDate;

    private ZonedDateTime modificationDate;

    private String deviceStored;

    private String storageLocation;

    Inventory(
        UnitNumber unitNumber,
        ProductCode productCode,
        String shortDescription,
        LocalDateTime expirationDate,
        String collectionDate,
        String location,
        ProductFamily productFamily,
        AboRhType aboRh) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.shortDescription = shortDescription;
        this.expirationDate = expirationDate;
        this.collectionDate = collectionDate;
        this.location = location;
        this.productFamily = productFamily;
        this.aboRh = aboRh;
        this.inventoryStatus = InventoryStatus.AVAILABLE;
        this.id = UUID.randomUUID();

    }
}
