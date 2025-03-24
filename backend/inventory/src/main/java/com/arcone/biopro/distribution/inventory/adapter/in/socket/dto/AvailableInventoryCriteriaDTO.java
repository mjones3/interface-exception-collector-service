package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record AvailableInventoryCriteriaDTO(String productFamily, AboRhCriteria bloodType, String temperatureCategory, boolean isLabeled, boolean isShortDate) implements Serializable {
}
