package com.arcone.biopro.distribution.orderservice.unit.infrastructure.service;

import com.arcone.biopro.distribution.orderservice.infrastructure.service.CustomerRsocketClient;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerByCodeRequestPayloadDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.controller.error.DataNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig
public class CustomerRsocketClientTest {

    @Mock
    private RSocketRequester rSocketRequester;

    @Mock
    private RSocketRequester.RequestSpec requestSpec;

    @Test
    public void testGetCustomerByCode() {
        var customer = CustomerDTO.builder()
            .externalId("externalId")
            .name("name")
            .code("code")
            .departmentCode("departmentCode")
            .departmentName("departmentName")
            .phoneNumber("phoneNumber")
            .active("active")
            .build();

        given(rSocketRequester.route("getCustomerByCode"))
            .willReturn(requestSpec);

        given(requestSpec.data(Mockito.any()))
            .willReturn(requestSpec);

        given(requestSpec.retrieveMono(CustomerDTO.class))
            .willReturn(Mono.just(customer));

        var client = new CustomerRsocketClient(rSocketRequester);
        var response = client.getCustomerByCode(
                CustomerByCodeRequestPayloadDTO.builder()
                    .code("code")
                    .build()
            );

        StepVerifier.create(response)
            .consumeNextWith(detail -> {
                assertEquals(customer.externalId(), "externalId");
                assertEquals(customer.name(), "name");
                assertEquals(customer.code(), "code");
                assertEquals(customer.departmentCode(), "departmentCode");
                assertEquals(customer.departmentName(), "departmentName");
                assertEquals(customer.phoneNumber(), "phoneNumber");
                assertEquals(customer.active(), "active");
            })
            .verifyComplete();
    }

    @Test
    public void testGetCustomerByCodeCustomerNotFoundException(){
        given(rSocketRequester.route("getCustomerByCode"))
            .willReturn(requestSpec);

        given(requestSpec.data(Mockito.any()))
            .willReturn(requestSpec);

        given(requestSpec.retrieveMono(CustomerDTO.class))
            .willReturn(Mono.error(new DataNotFoundException("code")));

        var client = new CustomerRsocketClient(rSocketRequester);
        var response = client.getCustomerByCode(
            CustomerByCodeRequestPayloadDTO.builder()
                .code("code")
                .build()
        );

        StepVerifier.create(response)
            .expectError()
            .verify();
    }

}
