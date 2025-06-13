package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CloseShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentProcessingEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CloseShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CloseShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloseShipmentUseCase implements CloseShipmentService {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    @Transactional
    public Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> closeShipment(CloseShipmentCommandInput closeShipmentCommandInput) {
        log.debug("Closing shipment {}", closeShipmentCommandInput);

        return recoveredPlasmaShippingRepository.findOneById(closeShipmentCommandInput.shipmentId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", closeShipmentCommandInput.shipmentId()))))
            .flatMap(shipment -> Mono.fromSupplier(() -> shipment.markAsProcessing(new CloseShipmentCommand(closeShipmentCommandInput.shipmentId()
                , closeShipmentCommandInput.employeeId(), closeShipmentCommandInput.locationCode(), closeShipmentCommandInput.shipDate()))))
            .flatMap(recoveredPlasmaShippingRepository::update)
            .flatMap(updatedShipment -> {
                applicationEventPublisher.publishEvent(new RecoveredPlasmaShipmentProcessingEvent(updatedShipment));
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.SHIPMENT_PROCESSING_SUCCESS.getMessage())
                            .code(UseCaseMessageType.SHIPMENT_PROCESSING_SUCCESS.getCode())
                            .type(UseCaseMessageType.SHIPMENT_PROCESSING_SUCCESS.getType())
                            .build())
                    .build())
                    , recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(updatedShipment)
                    ,null));

            })
            .onErrorResume(error -> {
                log.error("Error closing shipment {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(error.getMessage())
                            .code(16)
                            .type(UseCaseNotificationType.WARN)
                            .build())
                    .build()), null, null));
            });

    }
}
