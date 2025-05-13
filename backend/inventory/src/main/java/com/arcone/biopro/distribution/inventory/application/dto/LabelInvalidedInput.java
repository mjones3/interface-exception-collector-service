package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Builder
public record LabelInvalidedInput(
    String unitNumber,
    String productCode) {
}
