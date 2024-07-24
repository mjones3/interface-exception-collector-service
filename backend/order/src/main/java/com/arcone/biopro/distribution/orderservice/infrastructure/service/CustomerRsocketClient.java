package com.arcone.biopro.distribution.orderservice.infrastructure.service;

import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerByCodeRequestPayloadDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerRsocketClient {

    private final RSocketRequester rSocketRequester;

    public Mono<CustomerDTO> getCustomerByCode(CustomerByCodeRequestPayloadDTO request) {
        return rSocketRequester
            .route("getCustomerByCode")
            .data(request)
            .retrieveMono(CustomerDTO.class);
    }

}
