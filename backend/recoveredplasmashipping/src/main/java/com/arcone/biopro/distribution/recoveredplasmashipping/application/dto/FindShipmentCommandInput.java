package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record FindShipmentCommandInput(
        Long shipmentId,
        String locationCode,
        String employeeId
) implements Serializable {
}
