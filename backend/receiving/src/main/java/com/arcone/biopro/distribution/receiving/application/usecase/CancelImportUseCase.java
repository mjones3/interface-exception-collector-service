package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.CancelImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.service.CancelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelImportUseCase implements CancelImportService {

    private final ImportRepository importRepository;
    private static final String IMPORT_SHIPPING_INFORMATION_URL = "imports/imports-enter-shipment-information";

    @Override
    @Transactional
    public Mono<UseCaseOutput<Void>> cancelImport(CancelImportCommandInput cancelImportCommandInput) {
        return importRepository.findOneById(cancelImportCommandInput.importId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", cancelImportCommandInput.importId()))))
            .flatMap(pendingImport ->  importRepository.deleteOneById(pendingImport.validateCancel().getId()))
            .then(Mono.fromCallable(() -> new UseCaseOutput<Void>(List.of(UseCaseNotificationOutput
                .builder()
                .useCaseMessage(
                    UseCaseMessage
                        .builder()
                        .message(UseCaseMessageType.IMPORT_CANCELED_SUCCESS.getMessage())
                        .code(UseCaseMessageType.IMPORT_CANCELED_SUCCESS.getCode())
                        .type(UseCaseMessageType.IMPORT_CANCELED_SUCCESS.getType())
                        .build())
                .build()), null, Map.of("next", IMPORT_SHIPPING_INFORMATION_URL))))
            .onErrorResume(error -> {
                log.error("Error creating canceling import ", error);
                return Mono.just(new UseCaseOutput<Void>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(error.getMessage())
                            .code(13)
                            .type(UseCaseNotificationType.WARN)
                            .build())
                    .build()), null, null));
            });
    }
}
