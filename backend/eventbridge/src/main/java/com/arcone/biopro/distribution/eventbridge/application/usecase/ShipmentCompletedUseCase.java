package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.ShipmentCompletedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.service.ShipmentCompletedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentCompletedUseCase implements ShipmentCompletedService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ShipmentCompletedMapper shipmentCompletedMapper;

    @Override
    public Mono<Void> processCompletedShipmentEvent(ShipmentCompletedPayload shipmentCompletedEventPayloadDTO) {
        return publishShipmentCompletedOutboundEvent(shipmentCompletedMapper.toDomain(shipmentCompletedEventPayloadDTO));
    }

    private Mono<Void> publishShipmentCompletedOutboundEvent(ShipmentCompletedOutbound shipmentCompletedOutbound){
        applicationEventPublisher.publishEvent(new ShipmentCompletedOutboundEvent(shipmentCompletedOutbound));
        return Mono.empty();
    }
}
