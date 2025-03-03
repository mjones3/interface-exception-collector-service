package com.arcone.biopro.distribution.partnerorderprovider.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.controller.OrderInboundController;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.CancelOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.CancelOrderInboundService;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.OrderInboundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.ZonedDateTime;
import java.util.UUID;

class OrderInboundControllerTest {

    private OrderInboundService orderInboundService;
    private CancelOrderInboundService cancelOrderInboundService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        orderInboundService = Mockito.mock(OrderInboundService.class);
        cancelOrderInboundService = Mockito.mock(CancelOrderInboundService.class);
        webTestClient = WebTestClient.bindToController(new OrderInboundController(orderInboundService,cancelOrderInboundService)).build();
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

        webTestClient.post().uri("/v1/partner-order-provider/orders")
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

        webTestClient.post().uri("/v1/partner-order-provider/orders")
            .exchange()
            .expectStatus().is5xxServerError();
    }

    @Test
    public void shouldReceiveCancelInboundOrder(){

        var id = UUID.randomUUID();
        var response = ValidationResponseDTO.builder()
            .id(id)
            .status("ACCEPTED")
            .timestamp(ZonedDateTime.now())
            .build();

        Mockito.when(cancelOrderInboundService.receiveCancelOrderInbound(Mockito.any(CancelOrderInboundDTO.class))).thenReturn(response);

        webTestClient.patch().uri("/v1/partner-order-provider/orders/123/cancel")
            .exchange()
            .expectStatus().isAccepted()
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.timestamp").isNotEmpty()
            .jsonPath("$.status").isEqualTo("ACCEPTED")
            .jsonPath("$.id").isEqualTo(id.toString());


    }

    @Test
    public void shouldNotReceiveCancelInboundOrder(){

        Mockito.when(cancelOrderInboundService.receiveCancelOrderInbound(Mockito.any(CancelOrderInboundDTO.class))).thenThrow(new RuntimeException("Test"));

        webTestClient.patch().uri("/v1/partner-order-provider/orders/123/cancel")
            .exchange()
            .expectStatus().is5xxServerError();
    }

}
