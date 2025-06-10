package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.EnterShippingInformationCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface ShippingInformationService {

    Mono<UseCaseOutput<ShippingInformationOutput>> enterShippingInformation(EnterShippingInformationCommandInput enterShippingInformationCommandInput);
}
