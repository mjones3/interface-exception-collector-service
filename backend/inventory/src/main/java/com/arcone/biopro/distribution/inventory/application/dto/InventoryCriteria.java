package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;

public record InventoryCriteria(ProductFamily productFamily, AboRhCriteria aboRh) {
}
