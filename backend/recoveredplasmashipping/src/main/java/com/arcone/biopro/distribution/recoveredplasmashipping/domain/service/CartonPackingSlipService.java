package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonPackingSlipOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.GenerateCartonPackingSlipCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface CartonPackingSlipService {

    Mono<UseCaseOutput<CartonPackingSlipOutput>> generateCartonPackingSlip(GenerateCartonPackingSlipCommandInput generateCartonPackingSlipCommandInput);
}
