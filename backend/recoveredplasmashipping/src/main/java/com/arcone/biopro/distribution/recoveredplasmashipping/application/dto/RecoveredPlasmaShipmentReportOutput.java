package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record RecoveredPlasmaShipmentReportOutput(

    Long shipmentId,
    String shipmentNumber,
    String customerName,
    String location,
    String transportationReferenceNumber,
    String productType,
    LocalDate shipmentDate,
    String status

) implements Serializable {
}
