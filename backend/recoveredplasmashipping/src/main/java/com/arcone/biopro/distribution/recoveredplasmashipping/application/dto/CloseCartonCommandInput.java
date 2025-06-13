package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CloseCartonCommandInput(
    Long cartonId,
    String employeeId,
    String locationCode
) implements Serializable {
}
