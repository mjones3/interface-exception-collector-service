package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentClosedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.RecoveredPlasmaShipmentClosedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.RecoveredPlasmaShipmentClosedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.service.RecoveredPlasmaShipmentClosedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShipmentClosedUseCase implements RecoveredPlasmaShipmentClosedService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RecoveredPlasmaShipmentClosedMapper recoveredPlasmaShipmentClosedMapper;


    @Override
    public Mono<Void> processClosedShipmentEvent(RecoveredPlasmaShipmentClosedPayload recoveredPlasmaShipmentClosedPayload) {
        return Mono.fromSupplier(() -> recoveredPlasmaShipmentClosedMapper.toDomain(recoveredPlasmaShipmentClosedPayload))
            .flatMap(this::publishShipmentClosedOutboundEvent)
            .onErrorResume(error -> {
                log.error("Error processing shipment closed event: {}", error.getMessage());
                return Mono.error(error);
            });
    }

    private Mono<Void> publishShipmentClosedOutboundEvent(RecoveredPlasmaShipmentClosedOutbound recoveredPlasmaShipmentClosedOutbound){
        applicationEventPublisher.publishEvent(new RecoveredPlasmaShipmentClosedOutboundEvent(recoveredPlasmaShipmentClosedOutbound));
        return Mono.empty();
    }
}
