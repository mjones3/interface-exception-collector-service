package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import reactor.core.publisher.Mono;

public interface InventoryUpdatedService {

    Mono<Void> processInventoryUpdatedEvent(InventoryUpdatedPayload inventoryUpdatedEventPayloadDTO);
}
