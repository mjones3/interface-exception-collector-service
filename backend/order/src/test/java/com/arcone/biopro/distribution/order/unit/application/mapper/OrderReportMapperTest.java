package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.application.mapper.OrderReportMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = { OrderReportMapper.class })
class OrderReportMapperTest {

    @Autowired
    OrderReportMapper orderReportMapper;

    @Test
    void shouldMapToDTO() {
        var createDate = ZonedDateTime.now();
        var desireShipDate = LocalDate.now();
        var orderReport = new OrderReport(1L, 2L, "3", "STATUS", new OrderCustomerReport("CODE", "NAME"), new OrderPriorityReport("PRIORITY", "PRIORITY COLOR"), createDate, desireShipDate);

        var orderReportDTO = orderReportMapper.mapToDTO(orderReport);
        assertEquals(orderReport.getOrderId(), orderReportDTO.orderId());
        assertEquals(orderReport.getOrderNumber(), orderReportDTO.orderNumber());
        assertEquals(orderReport.getExternalId(), orderReportDTO.externalId());
        assertEquals(orderReport.getOrderStatus(), orderReportDTO.orderStatus());
        assertEquals(orderReport.getOrderCustomerReport().getName(), orderReportDTO.orderCustomerReport().name());
        assertEquals(orderReport.getOrderCustomerReport().getCode(), orderReportDTO.orderCustomerReport().code());
        assertEquals(orderReport.getOrderPriorityReport().getPriority(), orderReportDTO.orderPriorityReport().priority());
        assertEquals(orderReport.getOrderPriorityReport().getPriorityColor(), orderReportDTO.orderPriorityReport().priorityColor());
        assertEquals(createDate, orderReportDTO.createDate());
        assertEquals(desireShipDate, orderReportDTO.desireShipDate());
    }

}
