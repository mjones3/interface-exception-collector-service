package com.arcone.biopro.distribution.orderservice.unit.domain;

import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.orderservice.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, emptyList()), "orderNumber cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, emptyList()), "locationCode cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, emptyList()), "shipmentType cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", null, null, null, null, null, null, null, null, null, null, null, null, null, null, emptyList()), "shippingMethod cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", null, null, null, null, null, null, null, null, null, null, null, null, null, emptyList()), "shippingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", null, null, null, null, null, null, null, null, null, null, null, null, emptyList()), "billingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", null, null, null, null, null, null, null, null, null, null, null, emptyList()), "desiredShippingDate cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", null, null, null, null, null, null, null, null, emptyList()), "productCategory cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", null, null, null, null, null, null, emptyList()), "orderStatus cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", null, null, null, null, null, emptyList()), "orderPriority cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", null, null, null, null, emptyList()), "createEmployeeId cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null, emptyList()), "orderItems cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null, emptyList()));
        assertDoesNotThrow(() -> new Order(customerService, null, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null,
            singletonList(new OrderItem(null, 1L, "productFamily1", "bloodType1", 3, "comments1", null, null))
        ));
    }

    @Test
    void testExists() {
        var order = new Order(customerService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null,
            singletonList(new OrderItem(1L, 1L, "productFamily1", "bloodType1", 3, "comments1", null, null))
        );

        given(orderRepository.existsById(order.getId(), TRUE))
            .willReturn(Mono.just(true));

        StepVerifier.create(order.exists(orderRepository))
            .expectNext(true)
            .verifyComplete();
    }

}
