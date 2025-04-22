package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PackCartonItemRequestDTO(
    Long cartonId,
    String unitNumber,
    String productCode,
    String employeeId,
    String locationCode
) implements Serializable {
}
