package com.arcone.biopro.distribution.order.unit.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import graphql.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderPriorityTest {

    @Test
    public void shouldCreateOrderPriority() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getOrderNumber()).thenReturn(1);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST","TEST"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));
        var priority = new OrderPriority("TEST", lookupService);
        Assert.assertNotNull(priority);
        Assert.assertNotNull(priority.getDeliveryType());
        Assert.assertNotNull(priority.getPriority());
        Assertions.assertEquals(1,priority.getPriority());

    }

    @Test
    public void shouldNotCreateOrderPriorityWhenPriorityIsInvalid() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderPriority(null, lookupService));

        assertEquals("orderPriority cannot be null or blank", exception.getMessage());

    }

    @Test
    public void shouldNotCreateOrderPriorityWhenPriorityDoesNotExists() {

        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST2","TEST2"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderPriority("TEST", lookupService));

        assertEquals("Order Priority TEST is not valid", exception.getMessage());

    }

}
