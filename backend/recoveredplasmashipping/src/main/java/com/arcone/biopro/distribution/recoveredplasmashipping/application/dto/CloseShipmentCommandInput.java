package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record CloseShipmentCommandInput(
    Long shipmentId,
    String employeeId,
    String locationCode,
    LocalDate shipDate
) implements Serializable {
}
