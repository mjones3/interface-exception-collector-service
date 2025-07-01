package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderCustomerReportDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderPriorityReportDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderReportDTO;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import org.springframework.stereotype.Component;

@Component
public class OrderReportMapper {

    public OrderReportDTO mapToDTO(OrderReport orderReport) {
        return new OrderReportDTO(
            orderReport.getOrderId(),
            orderReport.getOrderNumber(),
            orderReport.getExternalId(),
            orderReport.getOrderStatus(),
            orderReport.getOrderCustomerReport() != null ? OrderCustomerReportDTO.builder()
                .code(orderReport.getOrderCustomerReport().getCode())
                .name(orderReport.getOrderCustomerReport().getName())
                .build() : null,
            OrderPriorityReportDTO.builder()
                .priority(orderReport.getOrderPriorityReport().getPriority())
                .priorityColor(orderReport.getOrderPriorityReport().getPriorityColor())
                .build(),
            orderReport.getCreateDate(),
            orderReport.getDesireShipDate()
        );
    }

}
