package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record FindShipmentRequestDTO(
        Long shipmentId,
        String locationCode,
        String employeeId
) implements Serializable {
}
