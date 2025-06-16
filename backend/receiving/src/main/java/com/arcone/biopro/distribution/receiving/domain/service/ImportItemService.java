package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.AddImportItemCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface ImportItemService {

    Mono<UseCaseOutput<ImportOutput>> createImportItem(AddImportItemCommandInput addImportItemCommandInput);
}
