package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RemoveCartonItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface RemoveCartonItemService {

    Mono<UseCaseOutput<CartonOutput>> removeCartonItem(RemoveCartonItemCommandInput removeCartonItemCommandInput);
}
