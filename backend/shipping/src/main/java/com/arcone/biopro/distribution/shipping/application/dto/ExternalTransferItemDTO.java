package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ExternalTransferItemDTO(
    Long id,
    Long externalTransferId,
    String unitNumber,
    String productCode,
    String productFamily,
    String createdByEmployeeId
) implements Serializable {
}
