package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CreateCartonService;
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
@RequiredArgsConstructor
@Slf4j
public class CreateCartonUseCase implements CreateCartonService {

    private final CartonRepository cartonRepository;
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CartonOutputMapper cartonOutputMapper;
    private final LocationRepository locationRepository;
    private static final String CARTON_DETAILS_PAGE = "/recovered-plasma/%s/carton-details";
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Mono<UseCaseOutput<CartonOutput>> createCarton(CreateCartonCommandInput createCartonCommandInput) {
        return Mono.fromCallable(() -> Carton.createNewCarton(new CreateCartonCommand(createCartonCommandInput.shipmentId(), createCartonCommandInput.employeeId()),recoveredPlasmaShippingRepository , cartonRepository,locationRepository))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(cartonRepository::create)
            .doOnSuccess(carton -> applicationEventPublisher.publishEvent(new RecoveredPlasmaCartonCreatedEvent(carton)))
            .flatMap(createdCarton -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_CREATED_SUCCESS.getMessage())
                            .code(UseCaseMessageType.CARTON_CREATED_SUCCESS.getCode())
                            .type(UseCaseMessageType.CARTON_CREATED_SUCCESS.getType())
                            .build())
                    .build())
                    , cartonOutputMapper.toOutput(createdCarton)
                    , Map.of("next", String.format(CARTON_DETAILS_PAGE, createdCarton.getId()))));
            }).onErrorResume(error -> {
                log.error("Not able to create carton {}",error.getMessage());

                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_GENERATION_ERROR.getMessage())
                            .code(UseCaseMessageType.CARTON_GENERATION_ERROR.getCode())
                            .type(UseCaseMessageType.CARTON_GENERATION_ERROR.getType())
                            .build())
                    .build()), null, null));
            });

    }
}
