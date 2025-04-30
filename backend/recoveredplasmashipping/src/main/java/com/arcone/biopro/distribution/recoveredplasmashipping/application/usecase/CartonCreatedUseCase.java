package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartonCreatedUseCase {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @EventListener
    public Mono<RecoveredPlasmaShipment> handleCartonCreatedEvent(RecoveredPlasmaCartonCreatedEvent event) {
        log.debug("Carton created event Triggered : {}", event);

        return recoveredPlasmaShippingRepository.findOneById(event.getPayload().getShipmentId())
            .flatMap(recoveredPlasmaShipment -> {
                return recoveredPlasmaShippingRepository.update(recoveredPlasmaShipment.markAsInProgress());
            })
            .onErrorResume(error -> {
                log.error("Not able to handle carton created event {}",error.getMessage());
                return Mono.empty();
            });

    }

}
