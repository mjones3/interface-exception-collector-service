package com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhCriteria;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record AvailableInventoryCriteriaDTO(String productFamily, AboRhCriteria bloodType, String temperatureCategory) implements Serializable {
}
