package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

@Builder
public record UnsuitableInput(
    String unitNumber,
    String productCode,
    String reasonKey) {


}

