package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTransferOrderNumberCommandInput;
import reactor.core.publisher.Mono;

public interface ValidateTransferOrderNumberService {

    Mono<UseCaseOutput<ShippingInformationOutput>> validateTransferOrderNumber(ValidateTransferOrderNumberCommandInput validateTransferOrderNumberCommandInput);
}
