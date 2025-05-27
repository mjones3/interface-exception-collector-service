package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ModifyShipmentRequestDTO(
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
