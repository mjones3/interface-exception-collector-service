package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
