package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CompleteExternalTransferCommandDTO(
    Long externalTransferId,
    String hospitalTransferId,
    String employeeId
) implements Serializable {
}
