package com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.domain.service.ShippingServiceService;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShippingServiceRequestDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShippingServiceResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for Shipping Service.
 */
@RestController
@RequestMapping("/v1/shipping-service")
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceResource {

    private final ShippingServiceService service;

    /**
     * {@code POST  /v1/shipping-service : Shipping Service
     *
     * @param dto
     * @return the {@link ResponseEntity} with status {@code 201 (Created)}.
     */
    @PostMapping
    public Mono<ResponseEntity<ShippingServiceResponseDTO>> createShippingService(@RequestBody ShippingServiceRequestDTO dto) {
        return service
            .create(dto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}
