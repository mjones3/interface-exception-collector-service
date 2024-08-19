package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderQueryCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QueryOrderByDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderQueryMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderReportEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.persistence.LookupEntity;
import com.arcone.biopro.distribution.order.infrastructure.persistence.OrderEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderQueryMapperTest {

    @Test
    public void shouldMapToDomain(){

        OrderQueryCommandDTO commandDTO = Mockito.mock(OrderQueryCommandDTO.class);
        Mockito.when(commandDTO.limit()).thenReturn(10);
        Mockito.when(commandDTO.locationCode()).thenReturn("123");

        QuerySortDTO querySortDTO = Mockito.mock(QuerySortDTO.class);

        QueryOrderByDTO queryOrderByDTO = Mockito.mock(QueryOrderByDTO.class);
        Mockito.when(queryOrderByDTO.direction()).thenReturn("ASC");
        Mockito.when(queryOrderByDTO.property()).thenReturn("test");

        Mockito.when(querySortDTO.orderByList()).thenReturn(List.of(queryOrderByDTO));

        Mockito.when(commandDTO.querySort()).thenReturn(querySortDTO);

        OrderQueryMapper orderQueryMapper = new OrderQueryMapper();
        var orderQuery = orderQueryMapper.mapToDomain(commandDTO);

        assertNotNull(orderQuery);
        assertEquals(10, orderQuery.getLimit());
        assertEquals("123", orderQuery.getLocationCode());
        assertNotNull(orderQuery.getQuerySort());
        assertNotNull(orderQuery.getQuerySort().getQueryOrderByList());
        assertEquals("ASC", orderQuery.getQuerySort().getQueryOrderByList().get(0).getDirection());
        assertEquals("test", orderQuery.getQuerySort().getQueryOrderByList().get(0).getProperty());

    }

    @Test
    public void shouldMapToDomainWhenSortNull(){

        OrderQueryCommandDTO commandDTO = Mockito.mock(OrderQueryCommandDTO.class);
        Mockito.when(commandDTO.limit()).thenReturn(10);
        Mockito.when(commandDTO.locationCode()).thenReturn("123");

        OrderQueryMapper orderQueryMapper = new OrderQueryMapper();
        var orderQuery = orderQueryMapper.mapToDomain(commandDTO);

        assertNotNull(orderQuery);
        assertEquals(10, orderQuery.getLimit());
        assertEquals("123", orderQuery.getLocationCode());
        assertNotNull(orderQuery.getQuerySort());

    }

    @Test
    public void shouldMapToDomainWhenLimitNull(){

        OrderQueryCommandDTO commandDTO = Mockito.mock(OrderQueryCommandDTO.class);
        Mockito.when(commandDTO.locationCode()).thenReturn("123");
        Mockito.when(commandDTO.limit()).thenReturn(null);

        OrderQueryMapper orderQueryMapper = new OrderQueryMapper();
        var orderQuery = orderQueryMapper.mapToDomain(commandDTO);

        assertNotNull(orderQuery);
        assertEquals(20, orderQuery.getLimit());
        assertEquals("123", orderQuery.getLocationCode());
        assertNotNull(orderQuery.getQuerySort());

    }

    @Test
    public void shouldMapToDto(){

        var orderReport = Mockito.mock(OrderReport.class);

        var customerReport = Mockito.mock(OrderCustomerReport.class);
        Mockito.when(customerReport.getCode()).thenReturn("code");
        Mockito.when(customerReport.getName()).thenReturn("name");
        Mockito.when(orderReport.getOrderCustomerReport()).thenReturn(customerReport);

        var orderPriority = Mockito.mock(OrderPriorityReport.class);
        Mockito.when(orderPriority.getPriority()).thenReturn("PRIORITY");
        Mockito.when(orderPriority.getPriorityColor()).thenReturn("COLOR");

        Mockito.when(orderReport.getOrderPriorityReport()).thenReturn(orderPriority);

        Mockito.when(orderReport.getOrderId()).thenReturn(1L);
        Mockito.when(orderReport.getOrderNumber()).thenReturn(1L);
        Mockito.when(orderReport.getExternalId()).thenReturn("externalId");
        Mockito.when(orderReport.getOrderStatus()).thenReturn("STATUS");

        Mockito.when(orderReport.getCreateDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(orderReport.getDesireShipDate()).thenReturn(LocalDate.now());


        OrderQueryMapper mapper = new OrderQueryMapper();

        var orderReportDTO = mapper.mapToDTO(orderReport);

        Assertions.assertNotNull(orderReportDTO);
        Assertions.assertEquals(1L,orderReportDTO.orderId());
        Assertions.assertEquals(1L,orderReportDTO.orderNumber());
        Assertions.assertEquals("STATUS",orderReportDTO.orderStatus());
        Assertions.assertEquals("externalId",orderReportDTO.externalId());
        Assertions.assertNotNull(orderReportDTO.desireShipDate());
        Assertions.assertNotNull(orderReportDTO.createDate());
        Assertions.assertEquals("COLOR",orderReportDTO.orderPriorityReport().priorityColor());
        Assertions.assertEquals("PRIORITY",orderReportDTO.orderPriorityReport().priority());
        Assertions.assertEquals("code",orderReportDTO.orderCustomerReport().code());
        Assertions.assertEquals("name",orderReportDTO.orderCustomerReport().name());


    }

}
