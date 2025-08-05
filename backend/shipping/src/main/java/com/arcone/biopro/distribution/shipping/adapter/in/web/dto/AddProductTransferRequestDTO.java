package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record AddProductTransferRequestDTO(
    Long externalTransferId,
    String unitNumber,
    String productCode,
    String employeeId
) implements Serializable {
}
