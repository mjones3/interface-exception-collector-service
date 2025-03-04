package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedOutboundPayload",
    title = "ShipmentCompletedOutboundPayload",
    description = "Shipment Completed Outbound Event Payload"
)
@Builder
public record ShipmentCompletedOutboundPayload(

    @Schema(
        name = "shipmentNumber",
        title = "Shipment Number",
        description = "The shipment Id",
        example = "8647394577655843000",
        requiredMode = REQUIRED
    )
    Long shipmentNumber,

    @Schema(
        name = "externalOrderId",
        title = "External Order Id",
        description = "The external order id",
        example = "EXTERNAL8647394577655843234-3982965305572823842",
        requiredMode = REQUIRED
    )
    String externalOrderId,

    @Schema(
        name = "shipmentLocationCode",
        title = "Shipment Location code",
        description = "The shipment location code",
        example = "123456789",
        requiredMode = REQUIRED
    )
    String shipmentLocationCode,

    @Schema(
        name = "shipmentLocationName",
        title = "Shipment Location name",
        description = "The shipment location name",
        example = "MDL Hub 1",
        requiredMode = REQUIRED
    )
    String shipmentLocationName,

    @Schema(
        name = "customerCode",
        title = "Customer code",
        description = "The shipment customer code",
        example = "1",
        requiredMode = REQUIRED
    )
    String customerCode,

    @Schema(
        name = "customerType",
        title = "Customer type",
        description = "The shipment customer type",
        example = "CUSTOMER_TYPE",
        requiredMode = NOT_REQUIRED
    )
    String customerType,

    @Schema(
        name = "shipmentDate",
        title = "Shipment date",
        description = "The shipment completed date",
        example = "2024-10-03T15:44:42.328353258Z",
        requiredMode = REQUIRED
    )
    ZonedDateTime shipmentDate,

    @Schema(
        name = "quantityShipped",
        title = "Quantity Shipped",
        description = "The shipped quantity",
        example = "1",
        requiredMode = REQUIRED
    )
    Integer quantityShipped,

    @Schema(
        name = "lineItems",
        title = "Line Items",
        description = "The shipment items",
        requiredMode = REQUIRED
    )
    List<ShipmentCompletedOutboundItem> lineItems,

    @Schema(
        name = "services",
        title = "Services",
        description = "The shipment services",
        requiredMode = NOT_REQUIRED
    )
    List<ShipmentCompletedOutboundService> services
) implements Serializable {

}
