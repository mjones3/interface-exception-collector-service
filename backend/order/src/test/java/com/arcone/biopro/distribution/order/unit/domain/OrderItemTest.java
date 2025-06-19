package com.arcone.biopro.distribution.order.unit.domain;

import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void testValidation() {
        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));

        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, null, null, null, 0, null, null, null,"",orderConfigService), "productFamily cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, "productFamily", null, null, 0,null, null, null,"",orderConfigService), "bloodType cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, "productFamily", "bloodType", null, 0,null, null, null,"",orderConfigService), "quantity cannot be null");
        assertDoesNotThrow(() -> new OrderItem(null, null, "productFamily", "bloodType", 3, 1,null, null, null,"",orderConfigService));
    }

    @Test
    void shouldNotDefineAvailableQuantity(){

        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));

        var item = new OrderItem(null, null, "productFamily", "bloodType", 3, 0, null, null, null,"",orderConfigService);

        assertThrows(IllegalArgumentException.class, () ->  item.defineAvailableQuantity(-1), "Quantity must not be null");
        assertThrows(IllegalArgumentException.class, () ->  item.defineAvailableQuantity(null), "Quantity must not be negative");


    }

    @Test
    void shouldDefineAvailableQuantity(){

        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));

        var item = new OrderItem(null, null, "productFamily", "bloodType", 3, 0,null, null, null,"",orderConfigService);
        item.defineAvailableQuantity(10);

        Assertions.assertEquals(10,item.getQuantityAvailable());


    }

    @Test
    void shouldGetRemainingQuantity(){

        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));

        var item = new OrderItem(null, null, "productFamily", "bloodType", 3, 1,null, null, null,"",orderConfigService);

        Assertions.assertEquals(2,item.getQuantityRemaining());
    }

}
