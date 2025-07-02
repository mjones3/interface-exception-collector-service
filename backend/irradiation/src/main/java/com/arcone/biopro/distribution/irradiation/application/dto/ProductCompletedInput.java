package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductCompletedInput(String unitNumber,
                                    String productCode,
                                    List<VolumeInput> volumes,
                                    AboRhType aboRh) {
}
