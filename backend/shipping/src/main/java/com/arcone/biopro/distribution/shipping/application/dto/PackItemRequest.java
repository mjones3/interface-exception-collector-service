package com.arcone.biopro.distribution.shipping.application.dto;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record PackItemRequest(
    @NotNull Long shipmentItemId,
    @NotNull  String unitNumber,
    @NotNull  String productCode,
    @NotNull  String locationCode,
    @NotNull  String employeeId,
    @NotNull VisualInspection visualInspection

) implements Serializable {
}
