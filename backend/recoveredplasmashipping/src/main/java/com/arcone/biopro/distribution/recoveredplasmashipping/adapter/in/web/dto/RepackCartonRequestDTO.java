package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record RepackCartonRequestDTO(
    Long cartonId,
    String employeeId,
    String locationCode,
    String comments
) implements Serializable {
}
