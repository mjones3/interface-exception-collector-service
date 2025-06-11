package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTemperatureCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ValidationResultOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.TemperatureValidator;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateTemperatureCommand;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateTemperatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateTemperatureUseCase implements ValidateTemperatureService {

    private final ProductConsequenceRepository productConsequenceRepository;
    private final ValidationResultOutputMapper validationResultOutputMapper;

    @Override
    public Mono<UseCaseOutput<ValidationResultOutput>> validateTemperature(ValidateTemperatureCommandInput validateTemperatureCommandInput) {

        return Mono.fromSupplier(() -> new ValidateTemperatureCommand(validateTemperatureCommandInput.temperature(), validateTemperatureCommandInput.temperatureCategory()))
            .flatMap(validateTemperatureCommand -> {
                    return productConsequenceRepository.findAllByProductCategoryAndResultProperty(validateTemperatureCommand.getTemperatureCategory(),"TEMPERATURE")
                        .collectList()
                        .flatMap(productConsequenceList -> {
                            return Mono.fromSupplier(() -> TemperatureValidator.validateTemperature(validateTemperatureCommand,productConsequenceList));
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
                                    .code(5)
                                    .type(UseCaseNotificationType.CAUTION)
                                    .build())
                            .build()), validationResultOutputMapper.toOutput(validationResult), null));
                }
            }).onErrorResume(error -> {
                log.error("Not able to validate temperature: {}",error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.VALIDATE_TEMPERATURE_SYSTEM_ERROR.getMessage())
                            .code(UseCaseMessageType.VALIDATE_TEMPERATURE_SYSTEM_ERROR.getCode())
                            .type(UseCaseMessageType.VALIDATE_TEMPERATURE_SYSTEM_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }
}
