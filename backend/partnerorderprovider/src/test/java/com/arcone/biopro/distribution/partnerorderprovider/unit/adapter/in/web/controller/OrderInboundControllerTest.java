package com.arcone.biopro.distribution.partnerorderprovider.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.controller.OrderInboundController;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.OrderInboundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.ZonedDateTime;
import java.util.UUID;

class OrderInboundControllerTest {

    private OrderInboundService orderInboundService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        orderInboundService = Mockito.mock(OrderInboundService.class);
        webTestClient = WebTestClient.bindToController(new OrderInboundController(orderInboundService)).build();
    }

    @Test
    public void shouldReceiveInboundOrder(){

        var id = UUID.randomUUID();
        var response = ValidationResponseDTO.builder()
            .id(id)
            .status("CREATED")
            .timestamp(ZonedDateTime.now())
            .build();

        Mockito.when(orderInboundService.receiveOrderInbound(Mockito.any(OrderInboundDTO.class))).thenReturn(response);

        webTestClient.post().uri("/v1/orders")
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.timestamp").isNotEmpty()
            .jsonPath("$.status").isEqualTo("CREATED")
            .jsonPath("$.id").isEqualTo(id.toString());


    }

    @Test
    public void shouldNotReceiveInboundOrder(){

        Mockito.when(orderInboundService.receiveOrderInbound(Mockito.any(OrderInboundDTO.class))).thenThrow(new RuntimeException("Test"));

        webTestClient.post().uri("/v1/orders")
            .exchange()
            .expectStatus().is5xxServerError();
    }

}
