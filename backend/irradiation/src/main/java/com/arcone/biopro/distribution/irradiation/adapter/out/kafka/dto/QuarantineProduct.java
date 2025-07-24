package com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Schema(
    name = "QuarantineProduct",
    title = "QuarantineProduct",
    description = "Message for product quarantine process"
)
public record QuarantineProduct(
    @Schema(
        title = "Products",
        description = "List of products to quarantine",
        requiredMode = REQUIRED
    )
    List<QuarantineProductInput> products,
    
    @Schema(
        title = "Triggered By",
        description = "System or process that triggered the quarantine",
        example = "IRRADIATION",
        requiredMode = REQUIRED
    )
    String triggeredBy,
    
    @Schema(
        title = "Reason Key",
        description = "Key identifying the reason for quarantine",
        example = "IRRADIATION_FAILED",
        requiredMode = REQUIRED
    )
    String reasonKey,
    
    @Schema(
        title = "Comments",
        description = "Additional comments about the quarantine",
        example = "Irradiation process failed due to equipment malfunction",
        requiredMode = NOT_REQUIRED
    )
    String comments,
    
    @Schema(
        title = "Performed By",
        description = "User who performed the quarantine action",
        example = "John Doe",
        requiredMode = REQUIRED
    )
    String performedBy
) implements Serializable {
}