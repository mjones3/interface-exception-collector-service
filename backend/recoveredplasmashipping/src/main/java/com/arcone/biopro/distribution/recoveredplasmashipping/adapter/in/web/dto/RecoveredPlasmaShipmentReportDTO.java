package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record RecoveredPlasmaShipmentReportDTO(

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
