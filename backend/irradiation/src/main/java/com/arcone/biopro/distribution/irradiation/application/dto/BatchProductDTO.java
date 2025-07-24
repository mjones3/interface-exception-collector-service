package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryQuarantineOutput;
import lombok.Builder;

import java.util.List;

@Builder
public record BatchProductDTO(
    String unitNumber,
    String productCode,
    String productFamily,
    String productDescription,
    String status,
    List<InventoryQuarantineOutput> quarantines
) {
}
