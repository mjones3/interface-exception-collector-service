package com.arcone.biopro.distribution.order.unit.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import graphql.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductCategoryTest {

    @Test
    public void shouldCreateProductCategory() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST","TEST"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));
        Assert.assertNotNull(new ProductCategory("TEST", lookupService));

    }

    @Test
    public void shouldNotCreateProductCategoryWhenCategoryIsInvalid() {
        LookupService lookupService = Mockito.mock(LookupService.class);
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ProductCategory(null, lookupService));

        assertEquals("productCategory cannot be null or blank", exception.getMessage());

    }

    @Test
    public void shouldNotCreateProductCategoryWhenCategoryDoesNotExists() {

        LookupService lookupService = Mockito.mock(LookupService.class);
        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(new LookupId("TEST2","TEST2"));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ProductCategory("TEST", lookupService));

        assertEquals("Product Category TEST is not valid", exception.getMessage());

    }
}
