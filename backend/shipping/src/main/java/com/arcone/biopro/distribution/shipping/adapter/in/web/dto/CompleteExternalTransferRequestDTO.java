package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CompleteExternalTransferRequestDTO(
    Long externalTransferId,
    String hospitalTransferId,
    String employeeId
) implements Serializable {
}
