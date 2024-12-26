package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import lombok.Builder;

@Builder
public record ProductConvertedInput(UnitNumber unitNumber, ProductCode productCode) {
}
