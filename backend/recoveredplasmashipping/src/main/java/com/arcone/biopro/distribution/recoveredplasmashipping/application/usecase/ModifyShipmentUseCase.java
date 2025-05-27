package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ModifyShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.ModifyShipmentInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.ModifyShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModifyShipmentUseCase implements ModifyShipmentService {

    private final ModifyShipmentInputMapper modifyShipmentInputMapper;
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CustomerService customerService;
    private final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    private final RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;


    @Override
    @Transactional
    public Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> modifyShipment(ModifyShipmentCommandInput modifyShipmentCommandInput) {
        return Mono.fromCallable(() -> modifyShipmentInputMapper.toModifyCommand(modifyShipmentCommandInput))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(modifyCommand -> recoveredPlasmaShippingRepository.findOneById(modifyCommand.getShipmentId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", modifyCommand.getShipmentId()))))
                .flatMap(recoveredPlasmaShipment -> Mono.fromSupplier(() -> recoveredPlasmaShipment.modifyShipment(modifyCommand,customerService,recoveredPlasmaShipmentCriteriaRepository)))
                .flatMap(recoveredPlasmaShipment -> recoveredPlasmaShippingRepository.createShipmentHistory(recoveredPlasmaShipment.getShipmentHistory())
                    .then(recoveredPlasmaShippingRepository.update(recoveredPlasmaShipment)))
                .flatMap(modifiedShipment -> {
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(UseCaseMessageType.SHIPMENT_MODIFIED_SUCCESS.getMessage())
                                .code(UseCaseMessageType.SHIPMENT_MODIFIED_SUCCESS.getCode())
                                .type(UseCaseMessageType.SHIPMENT_MODIFIED_SUCCESS.getType())
                                .build())
                        .build())
                        , recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(modifiedShipment)
                        , null));

                }))
                .onErrorResume(error -> {
                    log.error("Error modifying shipment", error);
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(error.getMessage())
                                .code(26)
                                .type(UseCaseNotificationType.WARN)
                                .build())
                        .build()), null, null));
                });
    }
}
