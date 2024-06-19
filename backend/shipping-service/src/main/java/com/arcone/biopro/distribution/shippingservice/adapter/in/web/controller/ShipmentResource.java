package com.arcone.biopro.distribution.shippingservice.adapter.in.web.controller;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shippingservice.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shippingservice.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import com.arcone.biopro.distribution.shippingservice.domain.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/v1/shipments/pack-item")
    public Mono<RuleResponseDTO> packItem(@Valid @RequestBody PackItemRequest packItemRequest) {
        log.info("Request to pack a product {}", packItemRequest);
        return shipmentService.packItem(packItemRequest);
    }
    @PostMapping("/v1/shipments/complete")
    public Mono<RuleResponseDTO> completeShipment(@Valid @RequestBody CompleteShipmentRequest completeShipmentRequest) {
        log.info("Request to complete a shipment {}", completeShipmentRequest);
        return shipmentService.completeShipment(completeShipmentRequest);
    }
}
