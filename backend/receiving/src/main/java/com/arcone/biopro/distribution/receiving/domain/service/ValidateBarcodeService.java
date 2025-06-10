package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateBarcodeCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import reactor.core.publisher.Mono;

public interface ValidateBarcodeService {

    Mono<UseCaseOutput<ValidationResultOutput>> validateBarcode(ValidateBarcodeCommandInput validateBarcodeCommandInput);
}
