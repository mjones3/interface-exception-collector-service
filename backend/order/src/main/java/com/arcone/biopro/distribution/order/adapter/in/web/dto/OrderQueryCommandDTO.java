package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderQueryCommandDTO (
    String locationCode,
    String orderNumber,
    QuerySortDTO querySort,
    Integer limit

) implements Serializable {

}
