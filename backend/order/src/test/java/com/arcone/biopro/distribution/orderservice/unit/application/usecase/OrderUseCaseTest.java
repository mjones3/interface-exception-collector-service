package com.arcone.biopro.distribution.orderservice.unit.application.usecase;

import com.arcone.biopro.distribution.orderservice.application.usecase.OrderUseCase;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.orderservice.domain.repository.OrderRepository;
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
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(classes = { OrderUseCase.class })
public class OrderUseCaseTest {

    @Autowired
    OrderService orderService;

    @MockBean
    CustomerService customerService;

    @MockBean
    OrderRepository orderRepository;

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
    void testFindAll() {
        var orders = this.createOrders(5);
        given(this.orderRepository.findAll())
            .willReturn(Flux.fromIterable(orders));

        StepVerifier.create(orderService.findAll())
            .expectNextCount(5)
            .verifyComplete();
    }

    @Test
    void testFindOneById() {
        var order = this.createOrder(1L);
        given(this.orderRepository.findOneById(1L))
            .willReturn(Mono.just(order));

        StepVerifier.create(orderService.findOneById(1L))
            .expectNext(order)
            .verifyComplete();
    }

    @Test
    void testInsert() {
        var mockOrder = mock(Order.class);
        var realOrder = createOrder(1L);

        given(orderRepository.insert(mockOrder)).willReturn(Mono.just(realOrder));

        StepVerifier.create(orderService.insert(mockOrder))
            .expectNext(realOrder)
            .verifyComplete();
    }

    private Order createOrder(Long orderId) {
        return new Order(
            customerService,
            orderId,
            orderId,
            "externalId",
            "locationCode",
            "shipmentType",
            "shippingMethod",
            "code",
            "code",
            LocalDate.now(),
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
                    orderId,
                    "productFamily1",
                    "bloodType1",
                    3,
                    "comments1",
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
                ),
                new OrderItem(
                    2L,
                    orderId,
                    "productFamily2",
                    "bloodType2",
                    5,
                    "comments2",
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
                )
            )
        );
    }

    private List<Order> createOrders(int quantity) {
        return IntStream.range(0, quantity)
            .mapToLong(Long::valueOf)
            .mapToObj(this::createOrder)
            .toList();
    }

}
