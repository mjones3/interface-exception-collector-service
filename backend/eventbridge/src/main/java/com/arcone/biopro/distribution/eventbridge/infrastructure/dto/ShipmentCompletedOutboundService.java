package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedOutboundService",
    title = "ShipmentCompletedOutboundService",
    description = "Shipment Completed Outbound Services Payload"
)
@Builder
public record ShipmentCompletedOutboundService(
    @Schema(
        name = "serviceItemCode",
        title = "Service Item Code",
        description = "The service item code",
        example = "CODE",
        requiredMode = REQUIRED
    )
    String serviceItemCode,

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
