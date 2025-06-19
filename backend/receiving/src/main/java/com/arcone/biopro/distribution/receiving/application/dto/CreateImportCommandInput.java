package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CreateImportCommandInput(

    String temperatureCategory,
    LocalDateTime transitStartDateTime,
    String transitStartTimeZone,
    LocalDateTime transitEndDateTime,
    String transitEndTimeZone,
    BigDecimal temperature,
    String thermometerCode,
    String locationCode,
    String comments,
    String employeeId

) implements Serializable {
}
