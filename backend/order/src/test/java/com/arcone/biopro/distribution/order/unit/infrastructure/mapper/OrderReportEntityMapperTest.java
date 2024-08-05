package com.arcone.biopro.distribution.order.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderReportEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.persistence.LookupEntity;
import com.arcone.biopro.distribution.order.infrastructure.persistence.OrderEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZonedDateTime;

class OrderReportEntityMapperTest {

    @Test
    public void shouldMapToDomain(){

        var orderEntity = Mockito.mock(OrderEntity.class);
        Mockito.when(orderEntity.getShippingCustomerCode()).thenReturn("code");
        Mockito.when(orderEntity.getShippingCustomerName()).thenReturn("name");
        Mockito.when(orderEntity.getId()).thenReturn(1L);
        Mockito.when(orderEntity.getOrderNumber()).thenReturn(1L);
        Mockito.when(orderEntity.getExternalId()).thenReturn("externalId");
        Mockito.when(orderEntity.getStatus()).thenReturn("STATUS");
        Mockito.when(orderEntity.getPriority()).thenReturn("PRIORITY");
        Mockito.when(orderEntity.getCreateDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(orderEntity.getDesiredShippingDate()).thenReturn(LocalDate.now());

        var lookupEntity = Mockito.mock(LookupEntity.class);
        Mockito.when(lookupEntity.getOptionValue()).thenReturn("optionValue");

        OrderReportEntityMapper mapper = new OrderReportEntityMapper();

        var domain = mapper.mapToDomain(orderEntity, lookupEntity);

        Assertions.assertNotNull(domain);
        Assertions.assertEquals(1L,domain.getOrderId());
        Assertions.assertEquals(1L,domain.getOrderNumber());
        Assertions.assertEquals("STATUS",domain.getOrderStatus());
        Assertions.assertEquals("STATUS",domain.getOrderStatus());
        Assertions.assertEquals("externalId",domain.getExternalId());
        Assertions.assertNotNull(domain.getDesireShipDate());
        Assertions.assertNotNull(domain.getCreateDate());
        Assertions.assertEquals("optionValue",domain.getOrderPriorityReport().getPriorityColor());
        Assertions.assertEquals("PRIORITY",domain.getOrderPriorityReport().getPriority());
        Assertions.assertEquals("code",domain.getOrderCustomerReport().getCode());
        Assertions.assertEquals("name",domain.getOrderCustomerReport().getName());
    }

}
