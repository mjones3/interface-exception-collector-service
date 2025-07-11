package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
public record OrderQueryCommandDTO (
    String locationCode,
    String orderUniqueIdentifier,
    List<String> orderStatus,
    List<String> deliveryTypes,
    List<String> customers,
    LocalDate createDateFrom,
    LocalDate createDateTo,
    LocalDate desireShipDateFrom,
    LocalDate desireShipDateTo,
    String shipmentType,
    QuerySortDTO querySort,
    Integer pageSize,
    Integer pageNumber
) implements Serializable {

}
