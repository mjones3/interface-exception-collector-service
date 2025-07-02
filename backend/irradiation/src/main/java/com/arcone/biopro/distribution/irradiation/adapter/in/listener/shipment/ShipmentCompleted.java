package com.arcone.biopro.distribution.irradiation.adapter.in.listener.shipment;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.ShipmentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
    name = "ShipmentCompleted",
    title = "ShipmentCompleted",
    description = "Message for completed shipment"
)
public record ShipmentCompleted(
    @Schema(description = "Shipment identifier")
    String shipmentId,

    @Schema(description = "Order number associated with the shipment")
    String orderNumber,

    @Schema(description = "Type of shipment")
    ShipmentType shipmentType,

    @Schema(description = "User who performed the shipment completion")
    String performedBy,

    @Schema(description = "List of line items in the shipment")
    List<LineItem> lineItems) {

    @Schema(
        name = "LineItem",
        description = "Line item details in the shipment"
    )
    public record LineItem(
        @Schema(description = "List of products in the line item")
        List<Product> products) {

        @Schema(
            name = "Product",
            description = "Product details in the line item"
        )
        public record Product(
            @Schema(description = "Unit number identifier")
            String unitNumber,

            @Schema(description = "Product code")
            String productCode) {
        }
    }
}

