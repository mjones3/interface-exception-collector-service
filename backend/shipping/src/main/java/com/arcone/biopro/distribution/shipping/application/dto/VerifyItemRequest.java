package com.arcone.biopro.distribution.shipping.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record VerifyItemRequest(
    @NotNull Long shipmentId,
    @NotNull  String unitNumber,
    @NotNull  String productCode,
    @NotNull  String employeeId

) implements Serializable {
}
