package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidateTransferOrderNumberRequestDTO(
    Long orderNumber,
    String employeeId,
    String locationCode
) implements Serializable {
}
