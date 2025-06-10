package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CloseCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CloseCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CloseCartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloseCartonUseCase implements CloseCartonService {

    private final CartonRepository cartonRepository;
    private final CartonOutputMapper cartonOutputMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private static final String SHIPMENT_DETAILS_URL = "/recovered-plasma/%s/shipment-details?print=true&closeCartonId=%s";


    @Override
    @Transactional
    public Mono<UseCaseOutput<CartonOutput>> closeCarton(CloseCartonCommandInput closeCartonCommandInput) {
        return cartonRepository.findOneById(closeCartonCommandInput.cartonId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", closeCartonCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier(() -> carton.close(new CloseCartonCommand(closeCartonCommandInput.cartonId(), closeCartonCommandInput.employeeId(), closeCartonCommandInput.locationCode()))))
            .flatMap(cartonRepository::update)
            .doOnSuccess(carton -> {
                    log.debug("Publishing carton closed event {}",carton);
                    applicationEventPublisher.publishEvent(new RecoveredPlasmaCartonClosedEvent(carton));
            })
            .flatMap(closedCarton -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_CLOSED_SUCCESS.getMessage())
                            .code(UseCaseMessageType.CARTON_CLOSED_SUCCESS.getCode())
                            .type(UseCaseMessageType.CARTON_CLOSED_SUCCESS.getType())
                            .build())
                    .build())
                    , cartonOutputMapper.toOutput(closedCarton)
                    , Map.of("next", String.format(SHIPMENT_DETAILS_URL, closedCarton.getShipmentId(), closedCarton.getId()))));
            })
            .onErrorResume(error -> {
                log.error("Error closing carton {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_CLOSED_ERROR.getMessage())
                            .code(UseCaseMessageType.CARTON_CLOSED_ERROR.getCode())
                            .type(UseCaseMessageType.CARTON_CLOSED_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }
}
