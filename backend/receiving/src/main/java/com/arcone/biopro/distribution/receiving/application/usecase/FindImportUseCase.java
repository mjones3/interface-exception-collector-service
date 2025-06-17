package com.arcone.biopro.distribution.receiving.application.usecase;


import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.service.FindImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindImportUseCase implements FindImportService {

    private final ImportRepository importRepository;
    private final ImportOutputMapper importOutputMapper;

    @Override
    public Mono<UseCaseOutput<ImportOutput>> findImportBydId(Long importId) {
        return importRepository.findOneById(importId)
            .subscribeOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", importId))))
             .flatMap(createImport -> {
                    return Mono.just(new UseCaseOutput<>(Collections.emptyList(), importOutputMapper.toOutput(createImport), null ));
                })
                .onErrorResume(error -> {
                    log.error("Error Getting Import By ID ", error);
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

                });
    }
}
