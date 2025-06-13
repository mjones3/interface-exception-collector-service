package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record RepackCartonCommandInput(
    Long cartonId,
    String employeeId,
    String locationCode,
    String comments
) implements Serializable {
}
