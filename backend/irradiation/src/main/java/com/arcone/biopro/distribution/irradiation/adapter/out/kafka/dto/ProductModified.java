package com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Schema(
    name = "ProductModified",
    title = "ProductModified",
    description = "Message for product modification events during irradiation process"
)
public record ProductModified(
    @Schema(
        title = "Unit Number",
        description = "Unique identifier for the product unit",
        example = "W777725001001",
        requiredMode = REQUIRED
    )
    String unitNumber,

    @Schema(
        title = "Product Code",
        description = "Original product code",
        example = "PROD123",
        requiredMode = REQUIRED
    )
    String productCode,

    @Schema(
        title = "Product Description",
        description = "Description of the product",
        example = "Blood Component - Red Blood Cells",
        requiredMode = NOT_REQUIRED
    )
    String productDescription,

    @Schema(
        title = "Parent Product Code",
        description = "New product code after modification",
        example = "PROD123_IRR",
        requiredMode = NOT_REQUIRED
    )
    String parentProductCode,

    @Schema(
        title = "Product Family",
        description = "Product family classification",
        example = "BLOOD_COMPONENTS",
        requiredMode = NOT_REQUIRED
    )
    String productFamily,

    @Schema(
        title = "Expiration Date",
        description = "Product expiration date",
        example = "2024-12-31",
        requiredMode = NOT_REQUIRED
    )
    String expirationDate,

    @Schema(
        title = "Expiration Time",
        description = "Product expiration time",
        example = "23:59:59",
        requiredMode = NOT_REQUIRED
    )
    String expirationTime,

    @Schema(
        title = "Modification Location",
        description = "Location where the modification occurred",
        example = "IRRADIATION_FACILITY_A",
        requiredMode = REQUIRED
    )
    String modificationLocation,

    @Schema(
        title = "Source",
        description = "Source system that performed the modification",
        example = "Irradiation Service",
        requiredMode = REQUIRED
    )
    String source,

    @Schema(
        title = "Imported Blood Center",
        description = "Information about the imported blood center",
        requiredMode = NOT_REQUIRED
    )
    ImportedBloodCenter importedBloodCenter
) implements Serializable {
}
