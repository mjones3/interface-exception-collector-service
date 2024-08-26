package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;

public record Product(String unitNumber, String productCode, String storageLocation, AboRhType aboRh) {
}
