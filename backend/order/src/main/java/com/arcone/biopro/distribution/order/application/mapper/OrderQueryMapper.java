package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderCustomerReportDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderPriorityReportDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderQueryCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderReportDTO;
import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderQueryMapper {

    public OrderQueryCommand mapToDomain(final OrderQueryCommandDTO commandDTO) {
        return new OrderQueryCommand(commandDTO.locationCode(),
            commandDTO.orderUniqueIdentifier(),
            commandDTO.orderStatus(),
            commandDTO.orderPriorities(),
            commandDTO.customers(),
            commandDTO.createDateFrom(),
            commandDTO.createDateTo(),
            commandDTO.desireShipDateFrom(),
            commandDTO.desireShipDateTo(),
            Optional.ofNullable(commandDTO.querySort()).map(querySortDTO -> new QuerySort(Optional
                .of(querySortDTO.orderByList().stream().map(sortDto -> new QueryOrderBy(sortDto.property(),sortDto.direction())).toList())
                .orElse(null))).orElse(null),
            commandDTO.limit());
    }

    public OrderReportDTO mapToDTO(final OrderReport orderReport ) {
        return new OrderReportDTO(orderReport.getOrderId(), orderReport.getOrderNumber()
            , orderReport.getExternalId(), orderReport.getOrderStatus(),
            OrderCustomerReportDTO.builder()
                .code(orderReport.getOrderCustomerReport().getCode())
                .name(orderReport.getOrderCustomerReport().getName())
                .build(), OrderPriorityReportDTO.builder()
            .priority(orderReport.getOrderPriorityReport().getPriority())
            .priorityColor(orderReport.getOrderPriorityReport().getPriorityColor())
            .build()
            , orderReport.getCreateDate(), orderReport.getDesireShipDate());
    }

}
