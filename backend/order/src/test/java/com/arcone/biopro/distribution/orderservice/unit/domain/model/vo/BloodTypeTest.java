package com.arcone.biopro.distribution.orderservice.unit.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import graphql.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BloodTypeTest {

    @Test
    public void shouldCreateBloodType() {
        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Assert.assertNotNull(new BloodType("TEST", "FAMILY_TEST" , orderConfigService));

    }

    @Test
    public void shouldNotCreateBloodTypeWhenTypeIsInvalid() {
        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new BloodType(null, "FAMILY_TEST" , orderConfigService));

        assertEquals("bloodType cannot be null or blank", exception.getMessage());

    }

    @Test
    public void shouldNotCreateBloodTypeWhenBloodTypeDoesNotExists() {

        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> new BloodType("TEST", "FAMILY_TEST" , orderConfigService));

        assertEquals("bloodType is not a valid blood type for this product family FAMILY_TEST", exception.getMessage());

    }
}
