package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.InventoryUpdatedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.InventoryUpdatedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.service.InventoryUpdatedService;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryUpdatedUseCase implements InventoryUpdatedService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final InventoryUpdatedMapper inventoryUpdatedMapper;

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "InventoryUpdatedOutbound",
        description = "Inventory Updated Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.InventoryUpdatedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "InventoryUpdatedOutbound",
            title = "InventoryUpdatedOutbound",
            description = "Inventory Updated Outbound Event"
        )
    ))
    @Override
    public Mono<Void> processInventoryUpdatedEvent(InventoryUpdatedPayload inventoryUpdatedEventPayloadDTO) {
        return publishInventoryUpdatedOutboundEvent(inventoryUpdatedMapper.toDomain(inventoryUpdatedEventPayloadDTO));
    }

    private Mono<Void> publishInventoryUpdatedOutboundEvent(InventoryUpdatedOutbound inventoryUpdatedOutbound){
        applicationEventPublisher.publishEvent(new InventoryUpdatedOutboundEvent(inventoryUpdatedOutbound));
        return Mono.empty();
    }
}
