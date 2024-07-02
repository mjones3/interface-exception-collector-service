package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CompleteShipmentRequest(
    Long shipmentId,
    String employeeId

) implements Serializable {
}
