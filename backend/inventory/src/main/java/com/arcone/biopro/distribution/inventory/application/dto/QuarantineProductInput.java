package com.arcone.biopro.distribution.inventory.application.dto;


import com.arcone.biopro.distribution.inventory.domain.model.enumeration.QuarantineReason;

public record QuarantineProductInput(Product product, QuarantineReason quarantineReason) {
}
