package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PackCartonItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface PackCartonItemService {
    Mono<UseCaseOutput<CartonOutput>> packItem(PackCartonItemCommandInput packCartonItemCommandInput);
}
