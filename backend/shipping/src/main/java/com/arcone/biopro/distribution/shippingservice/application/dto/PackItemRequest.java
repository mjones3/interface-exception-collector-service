package com.arcone.biopro.distribution.shippingservice.application.dto;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.VisualInspection;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record PackItemRequest(
    @NotNull Long shipmentItemId,
    @NotNull  String unitNumber,
    @NotNull  String productCode,
    @NotNull  Integer locationCode,
    @NotNull  String employeeId,
    @NotNull VisualInspection visualInspection

) implements Serializable {
}
