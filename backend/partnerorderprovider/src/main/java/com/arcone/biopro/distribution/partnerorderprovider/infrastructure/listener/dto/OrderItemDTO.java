package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderItemDTO (
    @Schema(
        title = "Product Family",
        description = "The product family",
        example = "PLASMA_TRANSFUSABLE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String productFamily,
    @Schema(
        title = "Blood Type",
        description = "The blood type",
        example = "AP",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String bloodType,
    @Schema(
        title = "Quantity",
        description = "The quantity",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer quantity,
    @Schema(
        title = "Comments",
        description = "The comments",
        example = "Item Comments",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String comments

) implements Serializable {


}
