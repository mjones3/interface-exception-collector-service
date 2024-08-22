package com.arcone.biopro.distribution.order.infrastructure.controller;

import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerByCodeRequestPayloadDTO;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
@Profile("customer-mock")
public class CustomerMockController {

    private final CustomerService customerService;

    @MessageMapping("getCustomerByCode")
    public Mono<CustomerDTO> getCustomerByCode(@Payload CustomerByCodeRequestPayloadDTO payload) {
        return this.customerService.getCustomerByCode(payload.code());
    }

}
