package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PackCartonItemCommandInput(
    Long cartonId,
    String unitNumber,
    String productCode,
    String employeeId,
    String locationCode
) implements Serializable {
}
