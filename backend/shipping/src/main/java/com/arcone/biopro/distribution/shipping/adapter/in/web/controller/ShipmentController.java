package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.domain.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;

    @QueryMapping("listShipments")
    public Flux<ShipmentResponseDTO> listShipments() {
        log.info("Requesting Pending order requests.....");
        return shipmentService.listShipments();
    }

    @QueryMapping("getShipmentDetailsById")
    public Mono<ShipmentDetailResponseDTO> getShipmentDetailsById(@Argument("shipmentId") long shipmentId) {
        log.info("Requesting Pending order requests.....");
        return shipmentService.getShipmentById(shipmentId);
    }

    @MutationMapping("packItem")
    public Mono<RuleResponseDTO> packItem(@Argument("packItemRequest") PackItemRequest packItemRequest) {
        log.info("Request to pack a product {}", packItemRequest);
        return shipmentService.packItem(packItemRequest);
    }
    @MutationMapping("completeShipment")
    public Mono<RuleResponseDTO> completeShipment(@Argument("completeShipmentRequest") CompleteShipmentRequest completeShipmentRequest) {
        log.info("Request to complete a shipment {}", completeShipmentRequest);
        return shipmentService.completeShipment(completeShipmentRequest);
    }
}
