package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderFulfilledMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShipmentService {

    Mono<Shipment> create(OrderFulfilledMessage message);

    Mono<ShipmentDetailResponseDTO> getShipmentById(Long shipmentId);

    Flux<ShipmentResponseDTO> listShipments();

}
