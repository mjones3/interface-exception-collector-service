package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Builder
public record OrderReportDTO (
    Long orderId,
    Long orderNumber,
    String externalId,
    String orderStatus,
    OrderCustomerReportDTO orderCustomerReport,
    OrderPriorityReportDTO orderPriorityReport,
    ZonedDateTime createDate,
    LocalDate desireShipDate
) implements Serializable {


}
