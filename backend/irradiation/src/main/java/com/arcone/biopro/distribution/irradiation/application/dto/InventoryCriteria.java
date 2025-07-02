package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhCriteria;

public record InventoryCriteria(String productFamily, AboRhCriteria aboRh, String temperatureCategory) {
}
