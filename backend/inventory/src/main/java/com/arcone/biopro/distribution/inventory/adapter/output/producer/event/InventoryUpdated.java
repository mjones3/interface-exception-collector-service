package com.arcone.biopro.distribution.inventory.adapter.output.producer.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(
    name = "InventoryUpdated",
    title = "InventoryUpdated",
    description = "Inventory Updated payload"
)
@Builder
public record InventoryUpdated(
    @Schema(
        name = "updateType",
        title = "Update Type",
        description = "The use case scenario that triggered the inventory updated event",
        example = "LABEL_APPLIED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String updateType,
    @Schema(
        name = "unitNumber",
        title = "Unit Number",
        description = "Unit Number of the updated inventory",
        example = "W036824111111",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String unitNumber,
    @Schema(
        name = "productCode",
        title = "Product Code",
        description = "Product Code of the updated inventory",
        example = "RBCAPH1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String productCode,
    @Schema(
        name = "productDescription",
        title = "Product Description",
        description = "Product Description of the updated inventory",
        example = "APH RBC B",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String productDescription,
    @Schema(
        name = "productFamily",
        title = "Product Family",
        description = "Product Family of the updated inventory",
        example = "RED_BLOOD_CELLS_LEUKOREDUCED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String productFamily,
    @Schema(
        name = "bloodType",
        title = "Blood Type",
        description = "Blood Type of the updated inventory",
        example = "AP",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String bloodType,
    @Schema(
        name = "expirationDate",
        title = "Expiration Date",
        description = "Expiration Date of the updated inventory",
        example = "2025-03-04"

    )
    LocalDate expirationDate,
    @Schema(
        name = "locationCode",
        title = "Location Code",
        description = "Location code of the updated inventory",
        example = "123456789"
    )
    String locationCode,
    @Schema(
        name = "storageLocation",
        title = "Storage Location",
        description = "Storage location of the updated inventory",
        example = "FREEZER 1|RACK 1|SHELF 1"
    )
    String storageLocation,
    @Schema(
        name = "inventoryStatus",
        title = "Inventory Statuses",
        description = "Current statuses of the updated inventory",
        example = "[\"AVAILABLE\"]"
    )
    List<String> inventoryStatus,
    @Schema(
        name = "properties",
        title = "Properties",
        description = "Properties of the updated inventory",
        example = "{ \"LICENSURE\": \"LICENSED\"}"
    )
    Map<String, Object> properties,
    @Schema(
        name = "inputProducts",
        title = "Input Products",
        description = "List of the parent product information",
        example = "[\n" +
            "    {\n" +
            "      \"unitNumber\": \"W123456748998\",\n" +
            "      \"productCode\": \"E468899\"\n" +
            "    }\n" +
            "  ]"
    )
    List<Object> inputProducts

) implements Serializable {
}
