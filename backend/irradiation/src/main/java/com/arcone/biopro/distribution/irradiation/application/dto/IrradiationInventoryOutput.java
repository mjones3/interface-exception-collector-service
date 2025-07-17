package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.InventoryQuarantine;
import lombok.Builder;

import java.util.List;

@Builder
public record IrradiationInventoryOutput(
    String unitNumber,
    String productCode,
    String location,
    String status,
    String productDescription,
    String productFamily,
    String shortDescription,
    boolean isLabeled,
    String statusReason,
    String unsuitableReason,
    Boolean expired,
    List<InventoryQuarantine> quarantines,
    boolean alreadyIrradiated,
    boolean notConfigurableForIrradiation) {
}

