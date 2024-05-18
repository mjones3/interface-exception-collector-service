package com.arcone.biopro.distribution.shippingservice.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.BioProApplication;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulFilledResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.usecase.OrderFulfilledServiceUseCase;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BioProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties =
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
    )
class OrderFulFilledResourceTest {

    @MockBean
    private OrderFulfilledServiceUseCase useCaseService;

    @Autowired
    private WebTestClient webClient;
    @Test
    @Disabled
    public void shouldListOrders(){

        OrderFulFilledResponseDTO dto = Mockito.mock(OrderFulFilledResponseDTO.class);
        Mockito.when(dto.orderNumber()).thenReturn(56L);
        Mockito.when(dto.status()).thenReturn(OrderStatus.OPEN);
        Mockito.when(dto.priority()).thenReturn(OrderPriority.ASAP);



        //Mockito.when(useCaseService.listOrderFulfilledRequests()).thenReturn(Mono.just(List.of(dto)));

        webClient
            .get().uri("/v1/orders")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(OrderFulFilledResponseDTO.class);
    }

}
