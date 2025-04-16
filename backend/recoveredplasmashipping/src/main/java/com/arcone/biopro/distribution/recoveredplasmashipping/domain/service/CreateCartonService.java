package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface CreateCartonService {

    Mono<UseCaseOutput<CartonOutput>> createCarton(CreateCartonCommandInput createCartonCommandInput);

}
