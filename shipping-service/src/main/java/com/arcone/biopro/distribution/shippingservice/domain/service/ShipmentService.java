package com.arcone.biopro.distribution.shippingservice.domain.service;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ShipmentService {

    Mono<Shipment> create(OrderFulfilledMessage message);

    Mono<ShipmentDetailResponseDTO> getShipmentById(Long shipmentId);

    Flux<ShipmentResponseDTO> listShipments();
}
