package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedOutboundItemProduct",
    title = "ShipmentCompletedOutboundItemProduct",
    description = "Shipment Completed Outbound Item Products Payload"
)
@Builder
public record ShipmentCompletedOutboundItemProduct(
    @Schema(
        name = "unitNumber",
        title = "Unit number",
        description = "The inventory outbound unit number",
        example = "W036898786800",
        requiredMode = REQUIRED
    )
    String unitNumber,

    @Schema(
        name = "productCode",
        title = "Product code",
        description = "The inventory outbound product code",
        example = "E7644V00",
        requiredMode = REQUIRED
    )
    String productCode,

    @Schema(
        name = "bloodType",
        title = "Blood type",
        description = "The donor blood type",
        example = "AB",
        requiredMode = REQUIRED
    )
    String bloodType,

    @Schema(
        name = "collectionDate",
        title = "Collection Date",
        description = "The donation collection date",
        example = "2011-12-03T09:15:30Z",
        requiredMode = REQUIRED
    )
    ZonedDateTime collectionDate,

    @Schema(
        name = "expirationDate",
        title = "Expiration Date",
        description = "The product expiration date",
        example = "2024-09-03T10:15:30",
        requiredMode = REQUIRED
    )
    LocalDateTime expirationDate,

    @Schema(
        name = "attributes",
        title = "Attributes",
        description = "The product attributes list",
        requiredMode = NOT_REQUIRED
    )
    Map<String,String> attributes
) implements Serializable {
}
