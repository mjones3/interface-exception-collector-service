package com.arcone.biopro.distribution.orderservice.unit.domain;

import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig
class OrderTest {

    @MockBean
    OrderRepository orderRepository;
    @MockBean
    CustomerService customerService;
    @MockBean
    OrderConfigService orderConfigService;
    @MockBean
    LookupService lookupService;

    @BeforeEach
    void beforeEach() {
        given(customerService.getCustomerByCode(anyString()))
            .willReturn(Mono.just(
                CustomerDTO.builder()
                    .code("code")
                    .name("name")
                    .build()
            ));
    }

    @Test
    @Disabled("Disabled until Manual Order Creation is implemented")
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "orderNumber cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "locationCode cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "shipmentType cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", null, null, null, null, null, null, null, null, null, null, null, null, null, null), "shippingMethod cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", null, null, null, null, null, null, null, null, null, null, null, null, null), "shippingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", null, null, null, null, null, null, null, null, null, null, null, null), "billingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", null, null, null, null, null, null, null, null, null, null, null), "desiredShippingDate cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", null, null, null, null, null, null, null, null), "productCategory cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", null, null, null, null, null, null), "orderStatus cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", null, null, null, null, null), "orderPriority cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", null, null, null, null), "createEmployeeId cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null), "orderItems cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null));
        assertDoesNotThrow(() -> new Order(customerService,lookupService, null, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null));
    }

    @Test
    @Disabled("Disabled until Manual Order Creation is implemented")
    void testExists() {
        var order = new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null);

        given(orderRepository.existsById(order.getId(), TRUE))
            .willReturn(Mono.just(true));

        StepVerifier.create(order.exists(orderRepository))
            .expectNext(true)
            .verifyComplete();
    }

}
