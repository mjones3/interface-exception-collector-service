package com.arcone.biopro.distribution.shippingservice.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller.OrderFulFilledResource;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulFilledResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulfilledDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.usecase.OrderFulfilledServiceUseCase;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import com.arcone.biopro.distribution.shippingservice.domain.service.OrderFulfilledService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

class OrderFulFilledResourceTest {

    private WebTestClient webTestClient;
    private OrderFulfilledService service;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(OrderFulfilledService.class);
        webTestClient = WebTestClient.bindToController(new OrderFulFilledResource(service)).build();
    }

    @Test

    public void shouldListOrders(){


        List<OrderFulFilledResponseDTO> responseDTOList = new ArrayList<>();
        responseDTOList.add(OrderFulFilledResponseDTO.builder()
            .id(1L)
                .orderNumber(56L)
            .build());
        Mockito.when(service.listOrderFulfilledRequests()).thenReturn(Mono.just(responseDTOList));

        webTestClient.get().uri("/v1/orders")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].id").isEqualTo(1L);


    }

    @Test
    public void shouldGetOrderByNumber(){

        Mockito.when(service.getOrderByNumber(1L)).thenReturn(Mono.just(OrderFulfilledDetailResponseDTO
            .builder()
                .id(1L)
                .orderNumber(56L)
            .build()));

        webTestClient.get().uri("/v1/orders/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1L);


    }

    @Test
    public void shouldReturnEmptyWhenOrderNumberDoesNotExist(){

        Mockito.when(service.getOrderByNumber(1L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/v1/orders/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody().isEmpty();

    }

}
