package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;


import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.GenerateCartonLabelCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LabelOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CartonLabelService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LabelTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCartonLabelUseCase implements CartonLabelService {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CartonRepository cartonRepository;
    private final LocationRepository locationRepository;
    private final SystemProcessPropertyRepository systemProcessPropertyRepository;
    private final LabelTemplateService labelTemplateService;

    @Override
    public Mono<UseCaseOutput<LabelOutput>> generateCartonLabel(GenerateCartonLabelCommandInput generateCartonLabelCommandInput) {
        return cartonRepository.findOneById(generateCartonLabelCommandInput.cartonId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", generateCartonLabelCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier( () -> carton.generateCartonLabel(labelTemplateService,locationRepository,recoveredPlasmaShippingRepository,systemProcessPropertyRepository)))
            .flatMap(cartonLabel -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_LABEL_GENERATED_SUCCESS.getMessage())
                            .code(UseCaseMessageType.CARTON_LABEL_GENERATED_SUCCESS.getCode())
                            .type(UseCaseMessageType.CARTON_LABEL_GENERATED_SUCCESS.getType())
                            .build())
                    .build())
                    , LabelOutput.builder().labelContent(cartonLabel).build()
                    , null));
            })
            .onErrorResume(error -> {
                log.error("Error generating carton Label {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_LABEL_GENERATED_ERROR.getMessage())
                            .code(UseCaseMessageType.CARTON_LABEL_GENERATED_ERROR.getCode())
                            .type(UseCaseMessageType.CARTON_LABEL_GENERATED_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }
}
