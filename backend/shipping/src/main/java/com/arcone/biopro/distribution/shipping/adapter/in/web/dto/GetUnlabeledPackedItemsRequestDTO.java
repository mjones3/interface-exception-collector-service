package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record GetUnlabeledPackedItemsRequestDTO(
    @NotNull Long shipmentId,
    @NotNull  String unitNumber
) implements Serializable {
}
