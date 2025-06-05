package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentClosedPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.RecoveredPlasmaShipmentClosedMapper;
import com.arcone.biopro.distribution.eventbridge.application.mapper.ShipmentCompletedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.RecoveredPlasmaShipmentClosedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.service.RecoveredPlasmaShipmentClosedService;
import com.arcone.biopro.distribution.eventbridge.domain.service.ShipmentCompletedService;
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
        return publishShipmentClosedOutboundEvent(recoveredPlasmaShipmentClosedMapper.toDomain(recoveredPlasmaShipmentClosedPayload));
    }

    private Mono<Void> publishShipmentClosedOutboundEvent(RecoveredPlasmaShipmentClosedOutbound recoveredPlasmaShipmentClosedOutbound){
        applicationEventPublisher.publishEvent(new RecoveredPlasmaShipmentClosedOutboundEvent(recoveredPlasmaShipmentClosedOutbound));
        return Mono.empty();
    }
}
