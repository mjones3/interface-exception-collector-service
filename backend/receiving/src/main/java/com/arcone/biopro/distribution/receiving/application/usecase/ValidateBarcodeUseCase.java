package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateBarcodeCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ValidationResultOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.BarcodeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateBarcodeCommand;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateBarcodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateBarcodeUseCase implements ValidateBarcodeService {
    private final ValidationResultOutputMapper validationResultOutputMapper;
    private final ConfigurationService configurationService;

    @Override
    public Mono<UseCaseOutput<ValidationResultOutput>> validateBarcode(ValidateBarcodeCommandInput validateBarcodeCommandInput) {
        return Mono.fromSupplier(() -> new ValidateBarcodeCommand(validateBarcodeCommandInput.barcodeValue(), validateBarcodeCommandInput.barcodePattern(), validateBarcodeCommandInput.temperatureCategory()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(validateBarcodeCommand -> Mono.fromSupplier(() -> BarcodeValidator.validateBarcode(validateBarcodeCommand,configurationService))
            ).flatMap(validationResult -> {
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
                                .type(UseCaseNotificationType.WARN)
                                .build())
                        .build()), validationResultOutputMapper.toOutput(validationResult), null));
                }
            }).onErrorResume(error -> {
                log.error("Not able to validate Barcode : {}",error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getMessage())
                            .code(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getCode())
                            .type(UseCaseMessageType.VALIDATE_BARCODE_SYSTEM_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }
}
