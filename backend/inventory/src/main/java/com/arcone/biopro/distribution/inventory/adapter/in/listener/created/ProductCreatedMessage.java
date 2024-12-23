package com.arcone.biopro.distribution.inventory.adapter.in.listener.created;

import com.arcone.biopro.distribution.inventory.application.dto.InputProduct;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;

import java.time.ZonedDateTime;
import java.util.List;

public record ProductCreatedMessage(
    String unitNumber,
    String productCode,
    String productDescription,
    String expirationDate,
    ValueUnit weight,
    ZonedDateTime drawTime,
    String manufacturingLocation,
    String productFamily,
    AboRhType aboRh,
    List<InputProduct> inputProducts) {
}
