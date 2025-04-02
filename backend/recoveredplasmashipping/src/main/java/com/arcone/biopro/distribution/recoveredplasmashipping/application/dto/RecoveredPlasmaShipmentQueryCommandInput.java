package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
public record RecoveredPlasmaShipmentQueryCommandInput(
    List<String> locationCode,
    String shipmentNumber,
    List<String> shipmentStatus,
    List<String> customers,
    List<String> productTypes,
    LocalDate shipmentDateFrom,
    LocalDate shipmentDateTo,
    QuerySortOutput querySort,
    Integer pageNumber,
    Integer pageSize
) implements Serializable {
}
