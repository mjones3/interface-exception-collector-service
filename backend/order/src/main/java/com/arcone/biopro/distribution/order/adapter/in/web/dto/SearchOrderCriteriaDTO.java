package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record SearchOrderCriteriaDTO(
    List<LookupDTO> orderStatus,
    List<LookupDTO> orderPriorities,
    List<OrderCustomerReportDTO> customers
) implements Serializable {


}
