package com.arcone.biopro.distribution.orderservice.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.controller.OrderController;
import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.OrderDTO;
import com.arcone.biopro.distribution.orderservice.application.mapper.OrderItemMapper;
import com.arcone.biopro.distribution.orderservice.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.orderservice.domain.model.Customer;
import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderCustomer;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderExternalId;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderNumber;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ShipmentType;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ShippingMethod;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderService;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
    void testFindAllOrders() {

        var order = mockOrder();

        given(this.orderService.findAll())
            .willReturn(Flux.just(order));

        // Act
        var response = this.orderController.findAllOrders()
            .toStream()
            .toArray(OrderDTO[]::new);

        // Assert
        assertEquals(response.length, 1);
    }

    @Test
    @Disabled("Disabled until Manual Order Creation is implemented")
    void testInsertOrder() {
        // Arrange
        var order = mockOrder();

        Lookup lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookup));


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
                lookupService,
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
                ZonedDateTime.now()
            )
        );
    }

    private Order mockOrder(){
        Order order = Mockito.mock(Order.class);
        Mockito.when(order.getOrderNumber()).thenReturn(Mockito.mock(OrderNumber.class));

        OrderExternalId orderExternalId = Mockito.mock(OrderExternalId.class);
        Mockito.when(orderExternalId.getOrderExternalId()).thenReturn("orderExternalId");
        Mockito.when(order.getOrderExternalId()).thenReturn(orderExternalId);

        ShipmentType shipmentType = Mockito.mock(ShipmentType.class);
        Mockito.when(shipmentType.getShipmentType()).thenReturn("shipmentType");
        Mockito.when(order.getShipmentType()).thenReturn(shipmentType);

        Mockito.when(order.getShippingMethod()).thenReturn(Mockito.mock(ShippingMethod.class));

        Mockito.when(order.getProductCategory()).thenReturn(Mockito.mock(ProductCategory.class));

        Mockito.when(order.getOrderStatus()).thenReturn(Mockito.mock(OrderStatus.class));
        Mockito.when(order.getOrderPriority()).thenReturn(Mockito.mock(OrderPriority.class));

        OrderCustomer customer = Mockito.mock(OrderCustomer.class);
        Mockito.when(order.getBillingCustomer()).thenReturn(customer);
        Mockito.when(order.getShippingCustomer()).thenReturn(customer);

        return order;
    }
}
