package com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import com.arcone.biopro.distribution.shippingservice.domain.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ShipmentResource {

    private final ShipmentService shipmentService;

    @GetMapping("/v1/shipments")
    public Flux<ShipmentResponseDTO> listShipments() {
        log.info("Requesting Pending order requests.....");
        return shipmentService.listShipments();
    }
    @GetMapping("/v1/shipments/{shipmentId}")
    public Mono<ShipmentDetailResponseDTO> getShipmentDetailsById(@PathVariable("shipmentId") long shipmentId) {
        log.info("Requesting Pending order requests.....");
        return shipmentService.getShipmentById(shipmentId);
    }
}
