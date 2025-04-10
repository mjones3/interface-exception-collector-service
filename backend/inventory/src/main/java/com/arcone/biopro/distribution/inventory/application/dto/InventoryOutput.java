package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
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
    String temperatureCategory,
    AboRhType aboRh,
    Integer weight,
    Boolean isLicensed,
    ZonedDateTime collectionDate,
    String productFamily,
    String shortDescription,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    String storageLocation,
    List<Volume> volumes) {
}

