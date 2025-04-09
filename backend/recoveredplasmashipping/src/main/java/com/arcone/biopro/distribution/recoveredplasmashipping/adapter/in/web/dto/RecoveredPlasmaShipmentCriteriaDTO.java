package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record RecoveredPlasmaShipmentCriteriaDTO(
    Integer id,
    String customerCode,
    String productType) implements Serializable {
}
