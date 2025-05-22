package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RemoveCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonRemovedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RemoveCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RemoveCartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoveCartonUseCase implements RemoveCartonService {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CartonRepository cartonRepository;
    private final CartonItemRepository cartonItemRepository;

    @Override
    public Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> removeCarton(RemoveCartonCommandInput removeCartonCommandInput) {
        return cartonRepository.findOneById(removeCartonCommandInput.cartonId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", removeCartonCommandInput.cartonId()))))
            .publishOn(Schedulers.boundedElastic())
            .flatMap(carton -> Mono.fromSupplier(() -> carton.removeCarton(new RemoveCartonCommand(removeCartonCommandInput.cartonId(), removeCartonCommandInput.employeeId()),recoveredPlasmaShippingRepository)))
            .flatMap(cartonRemoved  -> {
               return cartonRepository.update(cartonRemoved)
                   .flatMap(carton -> cartonItemRepository.deleteAllByCartonId(cartonRemoved.getId()))
                   .then(Mono.just(cartonRemoved));

                })
            .doOnSuccess(carton ->  applicationEventPublisher.publishEvent(new RecoveredPlasmaCartonRemovedEvent(carton)))
                .flatMap(carton -> recoveredPlasmaShippingRepository.findOneById(carton.getShipmentId())
                    .flatMap(recoveredPlasmaShipment -> recoveredPlasmaShippingRepository.update(recoveredPlasmaShipment.markAsReopen())))
            .flatMap(recoveredPlasmaShipment -> recoveredPlasmaShippingRepository.findOneById(recoveredPlasmaShipment.getId())
                .map(recoveredPlasmaShipmentOutputMapper::toRecoveredPlasmaShipmentOutput)
                .map(recoveredPlasmaShipmentOutput -> new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_REMOVED_SUCCESS.getMessage())
                            .code(UseCaseMessageType.CARTON_REMOVED_SUCCESS.getCode())
                            .type(UseCaseMessageType.CARTON_REMOVED_SUCCESS.getType())
                            .build())
                    .build())
                    , recoveredPlasmaShipmentOutput
                    , null)
                ))
            .onErrorResume(error -> {
                log.error("Not able to remove carton {}",error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_REMOVED_ERROR.getMessage())
                            .code(UseCaseMessageType.CARTON_REMOVED_ERROR.getCode())
                            .type(UseCaseMessageType.CARTON_REMOVED_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }
}
