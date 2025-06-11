package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record ImportItemDTO(
    Long id,
    Long importId,
    String visualInspection,
    String licenseStatus,
    String unitNumber,
    String productCode,
    String aboRh,
    LocalDateTime expirationDate,
    String productFamily,
    String productDescription,
    boolean isQuarantined
) implements Serializable {
}
