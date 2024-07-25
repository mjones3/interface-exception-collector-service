package com.arcone.biopro.distribution.orderservice.unit.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import graphql.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStatusTest {

    @Test
    public void shouldCreateOrderStatus() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST","TEST"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));
        Assert.assertNotNull(new OrderStatus("TEST", lookupService));

    }

    @Test
    public void shouldNotCreateOrderStatusWhenStatusIsInvalid() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderStatus(null, lookupService));

        assertEquals("orderStatus cannot be null or blank", exception.getMessage());

    }

    @Test
    public void shouldNotCreateOrderStatusWhenStatusDoesNotExists() {

        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST2","TEST2"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderStatus("TEST", lookupService));

        assertEquals("orderStatus is not a valid order status", exception.getMessage());

    }

}
