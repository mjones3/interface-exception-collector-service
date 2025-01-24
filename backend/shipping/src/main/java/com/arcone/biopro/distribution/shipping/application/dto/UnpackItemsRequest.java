package com.arcone.biopro.distribution.shipping.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record UnpackItemsRequest(
    @NotNull Long shipmentItemId,
    @NotNull  String locationCode,
    List<UnpackItemRequest> unpackItems,
    @NotNull  String employeeId
) implements Serializable {
}
