package com.arcone.biopro.distribution.shipping.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record UnpackItemRequest(
    @NotNull  String unitNumber,
    @NotNull  String productCode
) implements Serializable {
}
