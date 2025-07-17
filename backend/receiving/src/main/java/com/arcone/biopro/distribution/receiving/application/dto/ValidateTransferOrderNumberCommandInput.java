package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidateTransferOrderNumberCommandInput(
    Long orderNumber,
    String employeeId,
    String locationCode
) implements Serializable {
}
