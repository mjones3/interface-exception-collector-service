package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompletedItemProductPayload",
    title = "ShipmentCompletedItemProductPayload",
    description = "Shipment Completed Item Products Payload"
)
@Builder
public record ShipmentCompletedItemProductPayload(
    @Schema(
        name = "unitNumber",
        title = "Unit number",
        description = "The inventory unit number",
        example = "W036898786800",
        requiredMode = REQUIRED
    )
    String unitNumber,

    @Schema(
        name = "productFamily",
        title = "Product family",
        description = "The inventory product family",
        example = "PLASMA_TRANSFUSABLE",
        requiredMode = REQUIRED
    )
    String productFamily,

    @Schema(
        name = "productCode",
        title = "Product code",
        description = "The inventory product code",
        example = "E7644V00",
        requiredMode = REQUIRED
    )
    String productCode,

    @Schema(
        name = "aboRh",
        title = "ABO Rh",
        description = "The donor blood type",
        example = "AB",
        requiredMode = REQUIRED
    )
    String aboRh,

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
        name = "createDate",
        title = "Create Date",
        description = "The inventory create date",
        example = "2024-10-03T15:44:42.328889299Z",
        requiredMode = REQUIRED
    )
    ZonedDateTime createDate,

    @Schema(
        name = "attributes",
        title = "Attributes",
        description = "The product attributes list",
        requiredMode = NOT_REQUIRED
    )
    List<Map<String,String>> attributes
) implements Serializable {
}
