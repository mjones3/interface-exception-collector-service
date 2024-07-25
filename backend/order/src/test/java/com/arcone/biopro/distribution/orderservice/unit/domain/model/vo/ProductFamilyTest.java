package com.arcone.biopro.distribution.orderservice.unit.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.ProductFamily;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import graphql.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductFamilyTest {

    @Test
    public void shouldCreateProductFamily() {
        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Assert.assertNotNull(new ProductFamily("TEST", "CATEGORY_TEST" , orderConfigService));

    }

    @Test
    public void shouldNotCreateProductFamilyWhenFamilyIsInvalid() {
        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ProductFamily(null, "CATEGORY_TEST" , orderConfigService));

        assertEquals("productFamily cannot be null or blank", exception.getMessage());

    }

    @Test
    public void shouldNotCreateProductFamilyWhenFamilyDoesNotExists() {

        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ProductFamily("TEST", "CATEGORY_TEST" , orderConfigService));

        assertEquals("Invalid product family for the specified product category:CATEGORY_TEST", exception.getMessage());

    }
}
