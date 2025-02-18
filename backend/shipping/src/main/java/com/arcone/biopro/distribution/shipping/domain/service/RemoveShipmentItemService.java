package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.RemoveProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RemoveItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface RemoveShipmentItemService {

    Mono<RuleResponseDTO> removeItem(RemoveItemRequest removeItemRequest);
    Mono<RemoveProductResponseDTO> getNotificationDetailsByShipmentId(Long shipmentId);
}
