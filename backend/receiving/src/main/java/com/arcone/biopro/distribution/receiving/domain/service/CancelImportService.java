package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.CancelImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface CancelImportService {
    Mono<UseCaseOutput<Void>> cancelImport(CancelImportCommandInput cancelImportCommandInput);
}
