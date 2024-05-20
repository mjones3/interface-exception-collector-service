package com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulFilledResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulfilledDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.service.OrderFulfilledService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for Order Fulfilled Service.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderFulFilledResource {

    private final OrderFulfilledService service;

    @GetMapping("/v1/orders")
    public Mono<List<OrderFulFilledResponseDTO>> listOrders() {
        log.info("Requesting Pending order requests.....");
        return service.listOrderFulfilledRequests();
    }

    @GetMapping("/v1/orders/{orderNumber}")
    public Mono<OrderFulfilledDetailResponseDTO> getOrderByNumber(@PathVariable("orderNumber") long orderNumber) {
        log.info("Requesting Pending order requests.....");
        return service.getOrderByNumber(orderNumber);
    }
}
