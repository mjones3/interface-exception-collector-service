package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record AddProductTransferCommandDTO(
    Long externalTransferId,
    String unitNumber,
    String productCode,
    String employeeId

) implements Serializable {
}
