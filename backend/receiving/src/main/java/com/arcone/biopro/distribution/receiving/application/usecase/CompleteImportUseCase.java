package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.CompleteImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.event.ImportCompletedDomainEvent;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.service.CompleteImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteImportUseCase implements CompleteImportService {

    private final ImportRepository importRepository;
    private final ImportOutputMapper importOutputMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private static final String IMPORT_SHIPPING_INFORMATION_URL = "imports/imports-enter-shipment-information";

    @Override
    @Transactional
    public Mono<UseCaseOutput<ImportOutput>> completeImport(CompleteImportCommandInput completeImportCommandInput) {
        return importRepository.findOneById(completeImportCommandInput.importId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", completeImportCommandInput.importId()))))
            .flatMap(pendingImport ->  importRepository.update(pendingImport.completeImport(completeImportCommandInput.completeEmployeeId())))
            .doOnSuccess(completedImport -> {
                log.debug("Publishing complete import domain event {}",completedImport);
                applicationEventPublisher.publishEvent(new ImportCompletedDomainEvent(completedImport));
            } )
            .flatMap(completedImport -> {
                     return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                         .builder()
                         .useCaseMessage(
                             UseCaseMessage
                                 .builder()
                                 .message(UseCaseMessageType.IMPORT_COMPLETED_SUCCESS.getMessage())
                                 .code(UseCaseMessageType.IMPORT_COMPLETED_SUCCESS.getCode())
                                 .type(UseCaseMessageType.IMPORT_COMPLETED_SUCCESS.getType())
                                 .build())
                         .build()), importOutputMapper.toOutput(completedImport), Map.of("next", IMPORT_SHIPPING_INFORMATION_URL) ));
                })
                .onErrorResume(error -> {
                    log.error("Error creating completing import ", error);
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(error.getMessage())
                                .code(12)
                                .type(UseCaseNotificationType.WARN)
                                .build())
                        .build()), null, null));

                });
    }
}
