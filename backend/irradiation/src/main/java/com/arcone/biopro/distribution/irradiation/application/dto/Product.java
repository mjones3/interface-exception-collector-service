package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import lombok.Builder;

@Builder
public record Product(String unitNumber, String productCode, String storageLocation, AboRhType aboRh) {
}
