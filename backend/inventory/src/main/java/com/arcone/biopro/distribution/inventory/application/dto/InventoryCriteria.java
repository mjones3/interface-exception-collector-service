package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;

public record InventoryCriteria(String productFamily, AboRhCriteria aboRh, boolean isLabeled, boolean isShortDate, String temperatureCategory) {
}
