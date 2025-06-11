package com.arcone.biopro.distribution.receiving.adapter.in.web.controller;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.AddImportItemRequestDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.CreateImportRequestDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ImportDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.receiving.domain.service.ImportItemService;
import com.arcone.biopro.distribution.receiving.domain.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ImportController {

    private final ImportService importService;
    private final ImportItemService importItemService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final UseCaseResponseMapper useCaseResponseMapper;

    @MutationMapping("createImport")
    public Mono<UseCaseResponseDTO<ImportDTO>> createImport(@Argument("createImportRequest") CreateImportRequestDTO createImportRequest) {
        log.debug("Request to create a Import : {}", createImportRequest);
        return importService.createImport(commandRequestDTOMapper.toCommandInput(createImportRequest))
            .map(useCaseResponseMapper::toCreateImportUseCaseResponse);
    }

    @MutationMapping("createImportItem")
    public Mono<UseCaseResponseDTO<ImportDTO>> createImportItem(@Argument("createImportItemRequest") AddImportItemRequestDTO addImportItemRequest) {
        log.debug("Request to create a Import Item : {}", addImportItemRequest);
        return importItemService.createImportItem(commandRequestDTOMapper.toCommandInput(addImportItemRequest))
            .map(useCaseResponseMapper::toCreateImportUseCaseResponse);
    }
}
