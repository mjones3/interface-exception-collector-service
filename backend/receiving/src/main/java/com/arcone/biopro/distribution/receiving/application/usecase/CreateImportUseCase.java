package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.CreateImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.application.mapper.InputCommandMapper;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateImportUseCase implements ImportService {

    private final ImportRepository importRepository;
    private final ImportOutputMapper importOutputMapper;
    private final InputCommandMapper inputCommandMapper;
    private final ProductConsequenceRepository productConsequenceRepository;
    private final DeviceRepository deviceRepository;
    private static final String IMPORT_PRODUCT_INFORMATION_URL = "imports/%s/product-information";


    @Override
    @Transactional
    public Mono<UseCaseOutput<ImportOutput>> createImport(CreateImportCommandInput createImportCommandInput) {
            return Mono.fromCallable(() -> inputCommandMapper.toCommand(createImportCommandInput,productConsequenceRepository,deviceRepository))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(createCommand -> Mono.just(Import.create(createCommand,productConsequenceRepository))
                    .flatMap(importRepository::create)
                    .flatMap(createImport -> {
                        return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                            .builder()
                            .useCaseMessage(
                                UseCaseMessage
                                    .builder()
                                    .message(UseCaseMessageType.IMPORT_CREATE_SUCCESS.getMessage())
                                    .code(UseCaseMessageType.IMPORT_CREATE_SUCCESS.getCode())
                                    .type(UseCaseMessageType.IMPORT_CREATE_SUCCESS.getType())
                                    .build())
                            .build()), importOutputMapper.toOutput(createImport), Map.of("next", String.format(IMPORT_PRODUCT_INFORMATION_URL, createImport.getId()))));
                    })
                    .onErrorResume(error -> {
                        log.error("Error creating imports", error);
                        return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                            .builder()
                            .useCaseMessage(
                                UseCaseMessage
                                    .builder()
                                    .message(error.getMessage())
                                    .code(10)
                                    .type(UseCaseNotificationType.WARN)
                                    .build())
                            .build()), null, null));

                    }));


    }
}
