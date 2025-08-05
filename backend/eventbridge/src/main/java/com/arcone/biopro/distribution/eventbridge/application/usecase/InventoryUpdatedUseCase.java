package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.InventoryUpdatedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.InventoryUpdatedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.service.InventoryUpdatedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryUpdatedUseCase implements InventoryUpdatedService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final InventoryUpdatedMapper inventoryUpdatedMapper;

    @Override
    public Mono<Void> processInventoryUpdatedEvent(InventoryUpdatedPayload inventoryUpdatedEventPayloadDTO) {
        return publishInventoryUpdatedOutboundEvent(inventoryUpdatedMapper.toDomain(inventoryUpdatedEventPayloadDTO));
    }

    private Mono<Void> publishInventoryUpdatedOutboundEvent(InventoryUpdatedOutbound inventoryUpdatedOutbound){
        applicationEventPublisher.publishEvent(new InventoryUpdatedOutboundEvent(inventoryUpdatedOutbound));
        return Mono.empty();
    }
}
