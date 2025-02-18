package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
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
    QuerySortDTO querySort,
    Integer limit

) implements Serializable {

}
