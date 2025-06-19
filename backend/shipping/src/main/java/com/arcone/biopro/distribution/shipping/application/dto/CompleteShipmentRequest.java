package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CompleteShipmentRequest(
    Long shipmentId,
    String employeeId

) implements Serializable {
}
