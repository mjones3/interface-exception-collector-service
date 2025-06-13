package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record GenerateCartonPackingSlipRequestDTO(
    Long cartonId,
    String employeeId,
    String locationCode
) implements Serializable {
}
