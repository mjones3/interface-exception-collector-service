package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ProductImportedItemInput(String unitNumber,
                                       String productCode,
                                       String productDescription,
                                       LocalDateTime expirationDate,
                                       String inventoryLocation,
                                       String productFamily,
                                       AboRhType aboRh,
                                       Boolean licensed,
                                       List<AddQuarantineInput> quarantines) {
}
