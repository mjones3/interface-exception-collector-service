package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record AddImportItemCommandInput(
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
