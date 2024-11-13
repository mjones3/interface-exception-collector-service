package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Builder
public record OrderQueryCommandDTO (
    String locationCode,
    String orderUniqueIdentifier,
    List<String> orderStatus,
    List<String> orderPriorities,
    List<String> customers,
    Date createDateFrom,
    Date createDateTo,
    Date desireShipDateFrom,
    Date desireShipDateTo,
    QuerySortDTO querySort,
    Integer limit

) implements Serializable {

}
