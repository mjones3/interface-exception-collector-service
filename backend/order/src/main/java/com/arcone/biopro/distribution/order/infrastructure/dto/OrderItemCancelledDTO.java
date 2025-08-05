package com.arcone.biopro.distribution.order.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

@Schema(
    name = "OrderItemCancelled",
    title = "OrderItemCancelled",
    description = "Order Item cancelled"
)
@Builder
public record OrderItemCancelledDTO(
    @Schema(
        title = "Product Family",
        description = "The product family",
        example = "WHOLE_BLOOD",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String productFamily,
    @Schema(
        title = "Blood Type",
        description = "The blood type",
        example = "AB",
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
        title = "Quantity Shipped",
        description = "The quantity shipped",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer quantityShipped,
    @Schema(
        title = "Quantity Remaining",
        description = "The quantity remaining",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer quantityRemaining,
    @Schema(
        title = "Comments",
        description = "The comments",
        example = "Please send short dated",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String comments
) implements Serializable {
}
