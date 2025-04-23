package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedPayload",
    title = "ShipmentCompletedPayload",
    description = "Shipment Completed Event Payload"
)
@Builder
public record ShipmentCompletedPayload(

    @Schema(
        name = "shipmentId",
        title = "Shipment Id",
        description = "The shipment Id",
        example = "8647394577655843000",
        requiredMode = REQUIRED
    )
    Long shipmentId,

    @Schema(
        name = "orderNumber",
        title = "Order Number",
        description = "The order number",
        example = "3982965305572824000",
        requiredMode = REQUIRED
    )
    Long orderNumber,

    @Schema(
        name = "externalOrderId",
        title = "External Order Id",
        description = "The external order id",
        example = "EXTERNAL8647394577655843234-3982965305572823842",
        requiredMode = REQUIRED
    )
    String externalOrderId,

    @Schema(
        name = "performedBy",
        title = "Performed By",
        description = "Shipment performed By",
        example = "4c973896-5761-41fc-8217-07c5d13a004b",
        requiredMode = REQUIRED
    )
    String performedBy,

    @Schema(
        name = "locationCode",
        title = "Location code",
        description = "The shipment location code",
        example = "123456789",
        requiredMode = REQUIRED
    )
    String locationCode,

    @Schema(
        name = "locationName",
        title = "Location name",
        description = "The shipment location name",
        example = "MDL Hub 1",
        requiredMode = REQUIRED
    )
    String locationName,

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
        name = "createDate",
        title = "Create date",
        description = "The shipment completed create date",
        example = "2024-10-03T15:44:42.328353258Z",
        requiredMode = REQUIRED
    )
    ZonedDateTime createDate,
    @Schema(
        name = "deliveryType",
        title = "Delivery type",
        description = "The shipment delivery type",
        example = "ASAP",
        requiredMode = REQUIRED
    )
    String deliveryType,
    @Schema(
        name = "customerName",
        title = "Customer Name",
        description = "The Customer Name",
        example = "Customer Name",
        requiredMode = NOT_REQUIRED
    )
    String customerName,
    @Schema(
        name = "departmentCode",
        title = "Department Code",
        description = "The Department code",
        example = "12356",
        requiredMode = NOT_REQUIRED
    )
    String departmentCode,
    @Schema(
        name = "lineItems",
        title = "Line Items",
        description = "The shipment items",
        requiredMode = REQUIRED
    )
    List<ShipmentCompletedItemPayload> lineItems,

    @Schema(
        name = "services",
        title = "Services",
        description = "The shipment services",
        requiredMode = NOT_REQUIRED
    )
    List<ShipmentCompletedServicePayload> services
) implements Serializable {

}
