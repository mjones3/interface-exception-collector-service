package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;

import java.util.List;

public record InventoryFamily(String productFamily, AboRhCriteria aboRh, Long quantityAvailable, List<Product> shortDateProducts) {
}
