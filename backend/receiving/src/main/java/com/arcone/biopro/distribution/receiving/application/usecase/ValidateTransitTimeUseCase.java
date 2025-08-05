package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTemperatureCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTransitTimeCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ValidationResultOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.TemperatureValidator;
import com.arcone.biopro.distribution.receiving.domain.model.TransitTimeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateTemperatureCommand;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateTransitTimeCommand;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateTemperatureService;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateTransitTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateTransitTimeUseCase implements ValidateTransitTimeService {

    private final ProductConsequenceRepository productConsequenceRepository;
    private final ValidationResultOutputMapper validationResultOutputMapper;

    @Override
    public Mono<UseCaseOutput<ValidationResultOutput>> validateTransitTime(ValidateTransitTimeCommandInput validateTransitTimeCommandInput) {

        return Mono.fromSupplier(() -> new ValidateTransitTimeCommand(validateTransitTimeCommandInput.temperatureCategory(), validateTransitTimeCommandInput.startDateTime(), validateTransitTimeCommandInput.startTimeZone()
                , validateTransitTimeCommandInput.endDateTime(), validateTransitTimeCommandInput.endTimeZone()))
            .flatMap(validateTemperatureCommand -> {
                    return productConsequenceRepository.findAllByProductCategoryAndResultProperty(validateTemperatureCommand.getTemperatureCategory(),"TRANSIT_TIME")
                        .collectList()
                        .flatMap(productConsequenceList -> {
                            return Mono.fromSupplier(() -> TransitTimeValidator.validateTransitTime(validateTemperatureCommand,productConsequenceList));
                        });
                }).flatMap(validationResult -> {
                    if(validationResult.valid()){
                        return Mono.just(new UseCaseOutput<>(Collections.emptyList(), validationResultOutputMapper.toOutput(validationResult), null));
                    }else{
                        return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                            .builder()
                            .useCaseMessage(
                                UseCaseMessage
                                    .builder()
                                    .message(validationResult.message())
                                    .code(6)
                                    .type(UseCaseNotificationType.CAUTION)
                                    .build())
                            .build()), validationResultOutputMapper.toOutput(validationResult), null));
                }
            }).onErrorResume(error -> {
                log.error("Not able to validate transit time: {}",error.getMessage());
                if(error instanceof IllegalArgumentException){
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(error.getMessage())
                                .code(11)
                                .type(UseCaseNotificationType.WARN)
                                .build())
                        .build()), null, null));
                }else{
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(UseCaseMessageType.VALIDATE_TRANSIT_TIME_SYSTEM_ERROR.getMessage())
                                .code(UseCaseMessageType.VALIDATE_TRANSIT_TIME_SYSTEM_ERROR.getCode())
                                .type(UseCaseMessageType.VALIDATE_TRANSIT_TIME_SYSTEM_ERROR.getType())
                                .build())
                        .build()), null, null));
                }

            });
    }
}
