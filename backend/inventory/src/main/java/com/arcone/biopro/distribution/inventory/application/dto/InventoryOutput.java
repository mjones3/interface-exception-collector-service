package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record InventoryOutput(
    UUID id,
    String unitNumber,
    String productCode,
    InventoryStatus inventoryStatus,
    LocalDateTime expirationDate,
    String location,
    String productDescription,
    AboRhType aboRh,
    String collectionDate,
    ProductFamily productFamily,
    String shortDescription,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    String storageLocation) {
}

