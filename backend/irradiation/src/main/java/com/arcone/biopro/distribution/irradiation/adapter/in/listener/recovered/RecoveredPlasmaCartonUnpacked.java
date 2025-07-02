package com.arcone.biopro.distribution.irradiation.adapter.in.listener.recovered;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
    name = "RecoveredPlasmaCartonUnpacked",
    title = "RecoveredPlasmaCartonUnpacked",
    description = "Message for unpacked recovered plasma carton"
)
public record RecoveredPlasmaCartonUnpacked(
    @Schema(description = "Carton number identifier")
    String cartonNumber,

    @Schema(description = "Sequence number of the carton")
    int cartonSequence,

    @Schema(description = "Location code where carton was unpacked")
    String locationCode,

    @Schema(description = "Type of product")
    String productType,

    @Schema(description = "ID of employee who unpacked the carton")
    String unpackEmployeeId,

    @Schema(description = "Date when carton was unpacked")
    String unpackDate,

    @Schema(description = "Status of the carton")
    String status,

    @Schema(description = "Total number of products in carton")
    int totalProducts,

    @Schema(description = "List of unpacked products in the carton")
    List<PackedProductMessage> unpackedProducts
) {
}
