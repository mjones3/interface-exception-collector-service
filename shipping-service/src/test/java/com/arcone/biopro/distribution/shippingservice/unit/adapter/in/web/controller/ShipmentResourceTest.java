package com.arcone.biopro.distribution.shippingservice.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller.ShipmentResource;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ShipmentResourceTest {

    private WebTestClient webTestClient;
    private ShipmentService service;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(ShipmentService.class);
        webTestClient = WebTestClient.bindToController(new ShipmentResource(service)).build();
    }

    @Test
    public void shouldListShipments(){

        Mockito.when(service.listShipments()).thenReturn(Flux.just(ShipmentResponseDTO.builder()
            .id(1L)
            .orderNumber(56L)
            .build()));

        webTestClient.get().uri("/v1/shipments")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].id").isEqualTo(1L);


    }

    @Test
    public void shouldGetOrderByNumber(){

        Mockito.when(service.getShipmentById(1L)).thenReturn(Mono.just(ShipmentDetailResponseDTO
            .builder()
            .id(1L)
            .orderNumber(56L)
            .build()));

        webTestClient.get().uri("/v1/shipments/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1L);


    }

    @Test
    public void shouldReturnEmptyWhenOrderNumberDoesNotExist(){

        Mockito.when(service.getShipmentById(1L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/v1/shipments/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody().isEmpty();

    }
}
