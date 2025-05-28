package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record GenerateCartonLabelRequestDTO(
    Long cartonId,
    String employeeId
) implements Serializable {
}
