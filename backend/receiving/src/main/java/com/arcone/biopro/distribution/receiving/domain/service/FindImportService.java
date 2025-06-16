package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface FindImportService {

    Mono<UseCaseOutput<ImportOutput>> findImportBydId(final Long importId);
}
