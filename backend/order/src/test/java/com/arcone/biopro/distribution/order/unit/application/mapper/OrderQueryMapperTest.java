package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderQueryCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QueryOrderByDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderQueryMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderQueryMapperTest {

    @Test
    public void shouldMapToDomain(){

        OrderQueryCommandDTO commandDTO = Mockito.mock(OrderQueryCommandDTO.class);
        Mockito.when(commandDTO.pageSize()).thenReturn(10);
        Mockito.when(commandDTO.locationCode()).thenReturn("123");
        Mockito.when(commandDTO.orderUniqueIdentifier()).thenReturn("123");

        QuerySortDTO querySortDTO = Mockito.mock(QuerySortDTO.class);

        QueryOrderByDTO queryOrderByDTO = Mockito.mock(QueryOrderByDTO.class);
        Mockito.when(queryOrderByDTO.direction()).thenReturn("ASC");
        Mockito.when(queryOrderByDTO.property()).thenReturn("test");

        Mockito.when(querySortDTO.orderByList()).thenReturn(List.of(queryOrderByDTO));

        Mockito.when(commandDTO.querySort()).thenReturn(querySortDTO);

        OrderQueryMapper orderQueryMapper = new OrderQueryMapper();
        var orderQuery = orderQueryMapper.mapToDomain(commandDTO);

        assertNotNull(orderQuery);
        assertEquals(10, orderQuery.getPageSize());
        assertEquals("123", orderQuery.getLocationCode());
        assertNotNull(orderQuery.getQuerySort());
        assertNotNull(orderQuery.getQuerySort().getQueryOrderByList());
        assertEquals("ASC", orderQuery.getQuerySort().getQueryOrderByList().get(0).getDirection());
        assertEquals("test", orderQuery.getQuerySort().getQueryOrderByList().get(0).getProperty());

    }

    @Test
    public void shouldMapToDomainWhenSortNull(){

        OrderQueryCommandDTO commandDTO = Mockito.mock(OrderQueryCommandDTO.class);
        Mockito.when(commandDTO.pageSize()).thenReturn(10);
        Mockito.when(commandDTO.locationCode()).thenReturn("123");
        Mockito.when(commandDTO.orderUniqueIdentifier()).thenReturn("123");

        OrderQueryMapper orderQueryMapper = new OrderQueryMapper();
        var orderQuery = orderQueryMapper.mapToDomain(commandDTO);

        assertNotNull(orderQuery);
        assertEquals(10, orderQuery.getPageSize());
        assertEquals("123", orderQuery.getLocationCode());
        assertNotNull(orderQuery.getQuerySort());

    }

    @Test
    public void shouldMapToDomainWhenLimitNull(){

        OrderQueryCommandDTO commandDTO = Mockito.mock(OrderQueryCommandDTO.class);
        Mockito.when(commandDTO.locationCode()).thenReturn("123");
        Mockito.when(commandDTO.orderUniqueIdentifier()).thenReturn("123");
        Mockito.when(commandDTO.pageSize()).thenReturn(null);

        OrderQueryMapper orderQueryMapper = new OrderQueryMapper();
        var orderQuery = orderQueryMapper.mapToDomain(commandDTO);

        assertNotNull(orderQuery);
        assertEquals(20, orderQuery.getPageSize());
        assertEquals("123", orderQuery.getLocationCode());
        assertNotNull(orderQuery.getQuerySort());

    }

}
