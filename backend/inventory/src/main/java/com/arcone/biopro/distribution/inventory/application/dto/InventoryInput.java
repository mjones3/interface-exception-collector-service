package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Builder
public record InventoryInput(
    String unitNumber,
    String productCode,
    String shortDescription,
    LocalDateTime expirationDate,
    Boolean isLicensed,
    Integer weight,
    ZonedDateTime collectionDate,
    String location,
    ProductFamily productFamily,
    AboRhType aboRh) {
}
