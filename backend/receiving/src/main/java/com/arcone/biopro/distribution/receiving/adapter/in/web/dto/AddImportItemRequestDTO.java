package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record AddImportItemRequestDTO(
    Long importId,
    String unitNumber,
    String productCode,
    String aboRh,
    LocalDateTime expirationDate,
    String visualInspection,
    String licenseStatus,
    String employeeId

) implements Serializable {
}
