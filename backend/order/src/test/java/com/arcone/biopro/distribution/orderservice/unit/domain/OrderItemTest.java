package com.arcone.biopro.distribution.orderservice.unit.domain;

import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void testValidation() {
        OrderConfigService orderConfigService = Mockito.mock(OrderConfigService.class);
        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));

        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, null, null, null, null, null, null,"",orderConfigService), "productFamily cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, "productFamily", null, null, null, null, null,"",orderConfigService), "bloodType cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, "productFamily", "bloodType", null, null, null, null,"",orderConfigService), "quantity cannot be null");
        assertDoesNotThrow(() -> new OrderItem(null, null, "productFamily", "bloodType", 3, null, null, null,"",orderConfigService));
    }

}
