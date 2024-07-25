package com.arcone.biopro.distribution.orderservice.unit.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ShippingMethod;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import graphql.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;

class ShippingMethodTest {

    @Test
    public void shouldCreateShippingMethod() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST","TEST"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));
        Assert.assertNotNull(new ShippingMethod("TEST", lookupService));

    }

    @Test
    public void shouldNotCreateShippingMethodWhenMethodIsInvalid() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ShippingMethod(null, lookupService));

        assertEquals("shippingMethod cannot be null or blank", exception.getMessage());

    }

    @Test
    public void shouldNotCreateShippingMethodWhenMethodDoesNotExists() {

        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST2","TEST2"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ShippingMethod("TEST", lookupService));

        assertEquals("shippingMethod is not a valid order shipping method", exception.getMessage());

    }
}
