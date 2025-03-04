package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(
    name = "InventoryUpdatedPayload",
    title = "InventoryUpdatedPayload",
    description = "Inventory Updated Event Payload"
)
@Builder
public record InventoryUpdatedPayload(
    @Schema(
        name = "updateType",
        title = "Update type",
        description = "The inventory update type",
        example = "SHIPPED",
        requiredMode = REQUIRED
    )
    String updateType,

    @Schema(
        name = "unitNumber",
        title = "Unit number",
        description = "The inventory unit number",
        example = "W035625205983",
        requiredMode = REQUIRED
    )
    String unitNumber,

    @Schema(
        name = "productCode",
        title = "Product code",
        description = "The inventory product code",
        example = "E067800",
        requiredMode = REQUIRED
    )
    String productCode,

    @Schema(
        name = "productDescription",
        title = "Product description",
        description = "The inventory product description",
        example = "APH AS3 LR",
        requiredMode = REQUIRED
    )
    String productDescription,

    @Schema(
        name = "productFamily",
        title = "Product family",
        description = "The inventory product family",
        example = "RED_BLOOD_CELLS_LEUKOREDUCED",
        requiredMode = REQUIRED
    )
    String productFamily,

    @Schema(
        name = "bloodType",
        title = "Blood type",
        description = "The donor blood type",
        example = "OP",
        requiredMode = NOT_REQUIRED
    )
    String bloodType,

    @Schema(
        name = "expirationDate",
        title = "Expiration Date",
        description = "The inventory expiration date",
        example = "2025-02-26",
        requiredMode = REQUIRED
    )
    LocalDate expirationDate,

    @Schema(
        name = "locationCode",
        title = "Location code",
        description = "The inventory location code",
        example = "MIAMI",
        requiredMode = REQUIRED
    )
    String locationCode,

    @Schema(
        name = "storageLocation",
        title = "Storage location",
        description = "The inventory storage location",
        example = "REFRIG 1",
        requiredMode = NOT_REQUIRED
    )
    String storageLocation,

    @Schema(
        name = "inventoryStatus",
        title = "Inventory Statuses",
        description = "The inventory statuses",
        example = "[SHIPPED,IN_TRANSIT,CONVERTED]",
        requiredMode = REQUIRED
    )
    List<String> inventoryStatus,

    @Schema(
        name = "properties",
        title = "Inventory properties",
        description = "The inventory properties",
        example = "LICENSURE: LICENSED/UNLICENSED",
        requiredMode = REQUIRED
    )
    Map<String, Object> properties

) implements Serializable {

}
