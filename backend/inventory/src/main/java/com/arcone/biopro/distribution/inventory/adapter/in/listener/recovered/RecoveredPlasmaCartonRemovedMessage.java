package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
    name = "RecoveredPlasmaCartonRemoved",
    title = "RecoveredPlasmaCartonRemoved",
    description = "Message for removed recovered plasma carton"
)
public record RecoveredPlasmaCartonRemovedMessage(
    @Schema(description = "Carton number identifier")
    String cartonNumber,

    @Schema(description = "Sequence number of the carton")
    int cartonSequence,

    @Schema(description = "Location code where carton was removed from")
    String locationCode,

    @Schema(description = "Type of product")
    String productType,

    @Schema(description = "ID of employee who removed the carton")
    String removeEmployeeId,

    @Schema(description = "Date when carton was removed")
    String removeDate,

    @Schema(description = "Status of the carton")
    String status,

    @Schema(description = "Total number of products in carton")
    int totalProducts,

    @Schema(description = "Total weight of carton")
    double totalWeight,

    @Schema(description = "Total volume of carton")
    double totalVolume,

    @Schema(description = "List of packed products in the carton")
    List<PackedProductMessage> packedProducts
) {
}

