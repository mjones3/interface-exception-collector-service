package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.Property;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
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
    String collectionLocation,
    String collectionTimeZone,
    Boolean isLabeled,
    String statusReason,
    String comments,
    String deviceStored,
    String unsuitableReason,
    String cartonNumber,
    String modificationLocation,
    ZonedDateTime productModificationDate,
    Boolean expired,
    List<Volume> volumes,
    List<InputProduct> inputProducts,
    List<Quarantine> quarantines,
    List<Property> properties) {
}
