package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.FindShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentQueryCommandInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecoveredPlasmaShipmentUseCase implements RecoveredPlasmaShipmentService {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;
    private final RecoveredPlasmaShipmentQueryCommandInputMapper recoveredPlasmaShipmentQueryCommandInputMapper;

    @Override
    public Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> findOneById(FindShipmentCommandInput findShipmentCommandInput) {
       return Mono.fromCallable(() -> RecoveredPlasmaShipment.fromFindByCommand(recoveredPlasmaShipmentQueryCommandInputMapper.toModel(findShipmentCommandInput) , recoveredPlasmaShippingRepository))
           .subscribeOn(Schedulers.boundedElastic())
           .flatMap(recoveredPlasmaShipment -> {
               return Mono.just(new UseCaseOutput<>(null
                   , recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(recoveredPlasmaShipment)
                   , null));
           }).onErrorResume(error -> {
               return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                   .builder()
                   .useCaseMessage(
                       UseCaseMessage
                           .builder()
                           .message(error.getMessage())
                           .code(5)
                           .type(UseCaseNotificationType.WARN)
                           .build())
                   .build()), null, null));
           });
    }
}
