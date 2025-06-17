package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.AddImportItemCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.application.mapper.InputCommandMapper;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import com.arcone.biopro.distribution.receiving.domain.service.ImportItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddImportItemUseCase implements ImportItemService {

    private final ImportRepository importRepository;
    private final ImportOutputMapper importOutputMapper;
    private final InputCommandMapper inputCommandMapper;
    private final ProductConsequenceRepository productConsequenceRepository;
    private final ConfigurationService configurationService;

    @Override
    @Transactional
    public Mono<UseCaseOutput<ImportOutput>> createImportItem(AddImportItemCommandInput addImportItemCommandInput) {
        return importRepository.findOneById(addImportItemCommandInput.importId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", addImportItemCommandInput.importId()))))
            .flatMap(existingImport -> Mono.fromCallable(() -> inputCommandMapper.toCommand(addImportItemCommandInput))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(createCommand -> Mono.fromSupplier(() -> existingImport.createImportItem(createCommand, configurationService, productConsequenceRepository)))
                .flatMap(importRepository::createImportItem)
                .flatMap(createdItem -> {
                    return importRepository.findOneById(createdItem.getImportId())
                        .map(updatedImport -> new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                            .builder()
                            .useCaseMessage(
                                UseCaseMessage
                                    .builder()
                                    .message(UseCaseMessageType.IMPORT_ITEM_CREATE_SUCCESS.getMessage())
                                    .code(UseCaseMessageType.IMPORT_ITEM_CREATE_SUCCESS.getCode())
                                    .type(UseCaseMessageType.IMPORT_ITEM_CREATE_SUCCESS.getType())
                                    .build())
                            .build()), importOutputMapper.toOutput(updatedImport),null ));
                })

                .onErrorResume(error -> {
                    log.error("Error creating import item", error);
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

                }));


    }

}
