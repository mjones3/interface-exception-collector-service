package com.arcone.biopro.distribution.shippingservice.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller.ShipmentResource;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shippingservice.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shippingservice.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shippingservice.domain.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
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

    @Test
    public void shouldPackItem(){

        Mockito.when(service.packItem(Mockito.any())).thenReturn(Mono.just(RuleResponseDTO
            .builder()
                .ruleCode(HttpStatus.OK)
            .build()));

        webTestClient.post().uri("/v1/shipments/pack-item")
            .bodyValue(PackItemRequest.builder()
                .unitNumber("TEST")
                .visualInspection(VisualInspection.SATISFACTORY)
                .productCode("123")
                .employeeId("test")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.ruleCode").isEqualTo("OK");


    }

    @Test
    public void shouldCompleteShipment(){

        Mockito.when(service.completeShipment(Mockito.any())).thenReturn(Mono.just(RuleResponseDTO
            .builder()
            .ruleCode(HttpStatus.OK)
            .build()));

        webTestClient.post().uri("/v1/shipments/complete")
            .bodyValue(CompleteShipmentRequest.builder()
                .shipmentId(1L)
                .employeeId("employee-test")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.ruleCode").isEqualTo("OK");
    }
}
