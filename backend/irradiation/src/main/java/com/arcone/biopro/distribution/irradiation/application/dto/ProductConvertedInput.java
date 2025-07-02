package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.UnitNumber;
import lombok.Builder;

@Builder
public record ProductConvertedInput(UnitNumber unitNumber, ProductCode productCode) {
}
