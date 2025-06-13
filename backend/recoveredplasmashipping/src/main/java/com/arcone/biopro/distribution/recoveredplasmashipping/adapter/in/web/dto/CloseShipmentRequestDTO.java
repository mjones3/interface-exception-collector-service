package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record CloseShipmentRequestDTO(
    Long shipmentId,
    String employeeId,
    String locationCode,
    LocalDate shipDate
) implements Serializable {
}
