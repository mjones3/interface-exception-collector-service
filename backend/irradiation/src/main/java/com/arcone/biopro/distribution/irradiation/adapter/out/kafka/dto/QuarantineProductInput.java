package com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Schema(
    name = "QuarantineProductInput",
    title = "QuarantineProductInput",
    description = "Input for Product Quarantine"
)
public record QuarantineProductInput(
    @Schema(
        title = "Unit Number",
        description = "Unit Number of the product to quarantine",
        example = "W036825008001",
        requiredMode = REQUIRED
    )
    String unitNumber,
    
    @Schema(
        title = "Product Code",
        description = "Product code of the product to quarantine",
        example = "E0869V00",
        requiredMode = REQUIRED
    )
    String productCode
) implements Serializable {
}