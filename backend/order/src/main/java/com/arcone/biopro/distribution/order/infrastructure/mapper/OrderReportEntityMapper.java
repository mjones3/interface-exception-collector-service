package com.arcone.biopro.distribution.order.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import com.arcone.biopro.distribution.order.infrastructure.persistence.LookupEntity;
import com.arcone.biopro.distribution.order.infrastructure.persistence.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderReportEntityMapper {

    public OrderReport mapToDomain(OrderEntity orderEntity, String colorPriority) {
        return new OrderReport(
            orderEntity.getId(),
            orderEntity.getOrderNumber(),
            orderEntity.getExternalId(),
            orderEntity.getStatus(),
            new OrderCustomerReport(orderEntity.getShippingCustomerCode(), orderEntity.getShippingCustomerName()),
            new OrderPriorityReport(orderEntity.getDeliveryType(), colorPriority),
            orderEntity.getCreateDate(),
            orderEntity.getDesiredShippingDate()
        );
    }


}
