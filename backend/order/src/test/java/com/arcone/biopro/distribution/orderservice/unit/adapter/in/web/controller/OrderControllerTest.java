package com.arcone.biopro.distribution.orderservice.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.controller.OrderController;
import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.OrderDTO;
import com.arcone.biopro.distribution.orderservice.application.mapper.OrderItemMapper;
import com.arcone.biopro.distribution.orderservice.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderService;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig(classes = { OrderController.class, OrderMapper.class, OrderItemMapper.class })
class OrderControllerTest {

    @Autowired
    OrderController orderController;
    @Autowired
    OrderMapper orderMapper;
    @MockBean
    OrderService orderService;
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
    void testFindAllCustomers() {
        // Arrange
        var orders = getOrders();
        var orderIds = orders.stream()
            .map(Order::getId)
            .toList();

        given(this.orderService.findAll())
            .willReturn(Flux.fromIterable(orders));

        // Act
        var response = this.orderController.findAllOrders()
            .toStream()
            .toArray(OrderDTO[]::new);

        // Assert
        assertEquals(response.length, orders.size());
        assertTrue(
            Arrays.stream(response)
                .map(OrderDTO::id)
                .allMatch(orderIds::contains)
        );
    }

    @Test
    void testInsertOrder() {
        // Arrange
        var order = getOrders().getFirst();
        given(this.orderService.insert(any()))
            .willReturn(Mono.just(order));

        // Act
        var response = this.orderController
            .insertOrder(orderMapper.mapToDTO(order))
            .block();

        // Assert
        assertNotNull(response);
    }

    private List<Order> getOrders() {
        return List.of(
            new Order(
                customerService,
                1L,
                1L,
                "externalId",
                "locationCode",
                "shipmentType",
                "shippingMethod",
                "shippingCustomerCode",
                "billingCustomerCode",
                LocalDate.of(2023, Month.DECEMBER, 31),
                Boolean.TRUE,
                "phoneNumber",
                "productCategory",
                "comments",
                "status",
                "priority",
                "createEmployeeId",
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                List.of(
                    new OrderItem(
                        1L,
                        1L,
                        "productFamily1",
                        "bloodType1",
                        1,
                        "comments1",
                        ZonedDateTime.now(),
                        ZonedDateTime.now()
                    ),
                    new OrderItem(
                        2L,
                        1L,
                        "productFamily2",
                        "bloodType2",
                        1,
                        "comments2",
                        ZonedDateTime.now(),
                        ZonedDateTime.now()
                    )
                )
            )
        );
    }

}
