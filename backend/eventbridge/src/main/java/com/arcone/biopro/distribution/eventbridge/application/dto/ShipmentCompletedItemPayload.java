package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedItemPayload",
    title = "ShipmentCompletedItemPayload",
    description = "Shipment Completed Items Payload"
)
@Builder
public record ShipmentCompletedItemPayload(

    @Schema(
        name = "productFamily",
        title = "Product Family",
        description = "The shipment item product family",
        example = "PLASMA_TRANSFUSABLE",
        requiredMode = REQUIRED
    )
    String productFamily,

    @Schema(
        name = "quantity",
        title = "Quantity",
        description = "The shipment item quantity",
        example = "10",
        requiredMode = REQUIRED
    )
    Integer quantity,

    @Schema(
        name = "bloodType",
        title = "Blood Type",
        description = "The shipment item blood type",
        example = "AB",
        requiredMode = REQUIRED
    )
    String bloodType,

    @Schema(
        name = "products",
        title = "Products",
        description = "The shipment item product list",
        requiredMode = REQUIRED
    )
    List<ShipmentCompletedItemProductPayload> products

) implements Serializable {
}
