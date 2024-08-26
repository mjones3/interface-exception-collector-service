package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Inventory {

    UUID id;

    UnitNumber unitNumber;

    ProductCode productCode;

    String shortDescription;

    InventoryStatus inventoryStatus;

    LocalDateTime expirationDate;

    String collectionDate;

    String location;

    ProductFamily productFamily;

    String statusReason;

    AboRhType aboRh;

    ZonedDateTime createDate;

    ZonedDateTime modificationDate;

    List<Quarantine> quarantines;

    String comments;

    private String deviceStored;

    private String storageLocation;
}
