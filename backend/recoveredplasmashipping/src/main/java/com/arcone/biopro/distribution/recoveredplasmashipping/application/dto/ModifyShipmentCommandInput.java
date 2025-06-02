package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ModifyShipmentCommandInput(
    Long shipmentId,
    String modifyEmployeeId,
    String comments,
    String customerCode,
    String locationCode,
    String productType,
    String transportationReferenceNumber,
    LocalDate shipmentDate,
    BigDecimal cartonTareWeight

) implements Serializable {


}
