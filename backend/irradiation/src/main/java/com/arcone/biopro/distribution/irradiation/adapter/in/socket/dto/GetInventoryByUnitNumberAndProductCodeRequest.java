package com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import lombok.Builder;

@Builder
public record GetInventoryByUnitNumberAndProductCodeRequest(UnitNumber unitNumber,
                                                            String productCode) {
}
