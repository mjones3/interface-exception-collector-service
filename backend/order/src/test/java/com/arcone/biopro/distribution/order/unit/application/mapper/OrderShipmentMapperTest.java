package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderShipmentMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;

class OrderShipmentMapperTest {

    @Test
    public void shouldMapToDomain(){
        var target = new OrderShipmentMapper();
        var dto = Mockito.mock(ShipmentCreatedEvenPayloadDTO.class);
        Mockito.when(dto.orderNumber()).thenReturn(1L);
        Mockito.when(dto.shipmentId()).thenReturn(2L);
        Mockito.when(dto.shipmentStatus()).thenReturn("STATUS");

        var domain = target.mapToDomain(dto);

        Assertions.assertNotNull(domain);
        Assertions.assertEquals(1L, domain.getOrderId());
        Assertions.assertEquals(2L, domain.getShipmentId());
        Assertions.assertEquals("STATUS", domain.getShipmentStatus());
        Assertions.assertNotNull(domain.getCreateDate());
    }

    @Test
    public void shouldMapToDTO(){
        var target = new OrderShipmentMapper();
        var domain = Mockito.mock(OrderShipment.class);
        Mockito.when(domain.getId()).thenReturn(1L);
        Mockito.when(domain.getOrderId()).thenReturn(1L);
        Mockito.when(domain.getShipmentId()).thenReturn(2L);
        Mockito.when(domain.getShipmentStatus()).thenReturn("STATUS");
        Mockito.when(domain.getCreateDate()).thenReturn(ZonedDateTime.now());

        var dto = target.mapToDto(domain);

        Assertions.assertNotNull(domain);
        Assertions.assertEquals(1L, dto.id());
        Assertions.assertEquals(1L, dto.orderId());
        Assertions.assertEquals(2L, dto.shipmentId());
        Assertions.assertEquals("STATUS", dto.shipmentStatus());
        Assertions.assertNotNull(dto.createDate());
    }
}
