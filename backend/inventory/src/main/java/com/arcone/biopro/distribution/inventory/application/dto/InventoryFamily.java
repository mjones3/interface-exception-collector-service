package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;

import java.util.List;

public record InventoryFamily(ProductFamily productFamily, AboRhCriteria aboRh, Long quantityAvailable, List<Product> shortDateProducts) {
}
