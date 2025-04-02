package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
public record RecoveredPlasmaShipmentQueryCommandRequestDTO(

    List<String> locationCode,
    String shipmentNumber,
    List<String> shipmentStatus,
    List<String> customers,
    List<String> productTypes,
    LocalDate shipmentDateFrom,
    LocalDate shipmentDateTo,
    QuerySortDTO querySort,
    Integer pageNumber,
    Integer pageSize

) implements Serializable {
}
