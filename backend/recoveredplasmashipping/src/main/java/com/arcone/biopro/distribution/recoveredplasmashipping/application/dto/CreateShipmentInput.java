package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CreateShipmentInput(
    String customerCode,
    String locationCode,
    String productType,
    String createEmployeeId,
    String transportationReferenceNumber,
    LocalDate scheduleDate,
    BigDecimal cartonTareWeight

) implements Serializable {


}
