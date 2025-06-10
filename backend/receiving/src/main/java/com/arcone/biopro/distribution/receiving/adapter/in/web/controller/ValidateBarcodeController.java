package com.arcone.biopro.distribution.receiving.adapter.in.web.controller;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ValidateBarcodeRequestDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ValidationResultDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateBarcodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ValidateBarcodeController {

    private final UseCaseResponseMapper useCaseResponseMapper;
    private final ValidateBarcodeService validateBarcodeService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;

    @QueryMapping("validateBarcode")
    public Mono<UseCaseResponseDTO<ValidationResultDTO>> validateBarcode(@Argument("validateBarcodeRequest") ValidateBarcodeRequestDTO validateBarcodeRequest) {
        log.debug("Request to validate Barcode : {}", validateBarcodeRequest);
        return validateBarcodeService.validateBarcode(commandRequestDTOMapper.toCommandInput(validateBarcodeRequest))
            .map(useCaseResponseMapper::toValidateUseCaseResponse);
    }

}
