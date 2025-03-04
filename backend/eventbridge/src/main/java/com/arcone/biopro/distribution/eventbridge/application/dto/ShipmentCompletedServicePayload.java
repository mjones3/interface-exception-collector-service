package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedServicePayload",
    title = "ShipmentCompletedServicePayload",
    description = "Shipment Completed Services Payload"
)
@Builder
public record ShipmentCompletedServicePayload(
    @Schema(
        name = "code",
        title = "Code",
        description = "The service code",
        example = "CODE",
        requiredMode = REQUIRED
    )
    String code,

    @Schema(
        name = "quantity",
        title = "Quantity",
        description = "The service quantity",
        example = "1",
        requiredMode = REQUIRED
    )
    Integer quantity
) implements Serializable {
}
