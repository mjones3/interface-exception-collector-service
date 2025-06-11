package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record ImportItemOutput(
    Long id,
    Long importId,
    String visualInspection,
    String licenseStatus,
    String unitNumber,
    String productCode,
    String aboRh,
    LocalDateTime expirationDate,
    String productFamily,
    String productDescription
) implements Serializable {
}
