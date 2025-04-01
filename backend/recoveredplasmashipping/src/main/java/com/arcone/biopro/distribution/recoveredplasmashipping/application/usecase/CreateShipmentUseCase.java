package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateShipmentInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CreateShipmentInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CreateShipmentService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateShipmentUseCase implements CreateShipmentService {

    private final CreateShipmentInputMapper createShipmentInputMapper;
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CustomerService customerService;
    private final LocationRepository locationRepository;
    private final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    private final RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;
    private static final String SHIPMENT_DETAILS_URL = "/recovered-plasma/:%s/shipment-details";
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> createShipment(CreateShipmentInput createShipmentInput) {
        return Mono.fromCallable(() -> createShipmentInputMapper.toCreateCommand(createShipmentInput))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(createShipmentCommand -> Mono.fromSupplier(() -> RecoveredPlasmaShipment.createNewShipment(createShipmentCommand, customerService, recoveredPlasmaShippingRepository, locationRepository, recoveredPlasmaShipmentCriteriaRepository))
                .flatMap(recoveredPlasmaShippingRepository::create)
                .flatMap(createdShipment -> {
                    applicationEventPublisher.publishEvent(new RecoveredPlasmaShipmentCreatedEvent(createdShipment));
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(new UseCaseMessage(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS))
                        .build())
                        , recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(createdShipment)
                        , Map.of("next", String.format(SHIPMENT_DETAILS_URL, createdShipment.getId()))));

                })
                .onErrorResume(error -> {
                    log.error("Error creating shipment", error);
                    return Mono.just(buildErrorResponse(error));
                }));

    }

    private UseCaseOutput<RecoveredPlasmaShipmentOutput> buildErrorResponse(Throwable error) {
        return new UseCaseOutput<>(List.of(UseCaseNotificationOutput
            .builder()
            .useCaseMessage(new UseCaseMessage(3, UseCaseNotificationType.WARN, error.getMessage()))
            .build()), null, null);

    }


}
