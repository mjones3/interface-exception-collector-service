package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedOutboundItem",
    title = "ShipmentCompletedOutboundItem",
    description = "Shipment Completed Outbound Items Payload"
)
@Builder
public record ShipmentCompletedOutboundItem(

    @Schema(
        name = "productFamily",
        title = "Product Family",
        description = "The shipment outbound item product family",
        example = "PLASMA_TRANSFUSABLE",
        requiredMode = REQUIRED
    )
    String productFamily,

    @Schema(
        name = "qtyOrdered",
        title = "Quantity ordered",
        description = "The shipment outbound item quantity ordered",
        example = "10",
        requiredMode = REQUIRED
    )
    Integer qtyOrdered,

    @Schema(
        name = "qtyFilled",
        title = "Quantity filled",
        description = "The shipment outbound item quantity filled",
        example = "5",
        requiredMode = REQUIRED
    )
    Integer qtyFilled,

    @Schema(
        name = "products",
        title = "Products",
        description = "The shipment outbound item product list",
        requiredMode = REQUIRED
    )
    List<ShipmentCompletedOutboundItemProduct> products

) implements Serializable {
}
