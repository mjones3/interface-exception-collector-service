package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CreateCartonCommandInput(
    Long shipmentId,
    String employeeId
) implements Serializable {
}
