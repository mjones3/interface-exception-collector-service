package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;

public record InventoryInput(
    String unitNumber,
    String productCode,
    String shortDescription,
    String expirationDate,
    String collectionDate,
    String location,
    ProductFamily productFamily,
    AboRhType aboRh) {
}
