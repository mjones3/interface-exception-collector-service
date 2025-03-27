package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductCompletedInput(String unitNumber,
                                    String productCode,
                                    List<VolumeInput> volumes
                                   ) {
}
