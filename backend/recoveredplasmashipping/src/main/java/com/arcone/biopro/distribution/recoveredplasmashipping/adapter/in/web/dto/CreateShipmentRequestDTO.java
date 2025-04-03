package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CreateShipmentRequestDTO(
        String locationCode,
        String customerCode,
        String productType,
        String transportationReferenceNumber,
        LocalDate scheduleDate,
        BigDecimal cartonTareWeight,
        String createEmployeeId
) implements Serializable {
}
