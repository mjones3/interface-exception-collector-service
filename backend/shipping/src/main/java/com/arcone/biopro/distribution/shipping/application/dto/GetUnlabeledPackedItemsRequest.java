package com.arcone.biopro.distribution.shipping.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record GetUnlabeledPackedItemsRequest(
    @NotNull Long shipmentId,
    @NotNull  String unitNumber
) implements Serializable {
}
