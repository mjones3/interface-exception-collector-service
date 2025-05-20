package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RepackCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonUnpackedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RepackCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RepackCartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepackCartonUseCase implements RepackCartonService {

    private final CartonRepository cartonRepository;
    private final CartonOutputMapper cartonOutputMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private static final String CARTON_DETAILS_PAGE = "/recovered-plasma/%s/carton-details";
    private final CartonItemRepository cartonItemRepository;


    @Override
    public Mono<UseCaseOutput<CartonOutput>> repackCarton(RepackCartonCommandInput repackCartonCommandInput) {
        return cartonRepository.findOneById(repackCartonCommandInput.cartonId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", repackCartonCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier(() -> carton.markAsReopen(new RepackCartonCommand(repackCartonCommandInput.cartonId(), repackCartonCommandInput.employeeId(), repackCartonCommandInput.locationCode(),repackCartonCommandInput.comments()))))
            .flatMap(carton -> {
                log.debug("Publishing carton repacked event {}",carton);
                applicationEventPublisher.publishEvent(new RecoveredPlasmaCartonUnpackedEvent(carton));
                return cartonRepository.update(carton);
            }).flatMap(carton -> cartonItemRepository.deleteAllByCartonId(carton.getId())
                    .then(cartonRepository.findOneById(carton.getId())))
            .flatMap(repackedCarton -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_REPACKED_SUCCESS.getMessage())
                            .code(UseCaseMessageType.CARTON_REPACKED_SUCCESS.getCode())
                            .type(UseCaseMessageType.CARTON_REPACKED_SUCCESS.getType())
                            .build())
                    .build())
                    , cartonOutputMapper.toOutput(repackedCarton)
                    , Map.of("next", String.format(CARTON_DETAILS_PAGE,repackedCarton.getId()))));
            })
            .onErrorResume(error -> {
                log.error("Error closing carton {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(error.getMessage())
                            .code(20)
                            .type(UseCaseNotificationType.WARN)
                            .build())
                    .build()), null, null));
            });
    }
}
