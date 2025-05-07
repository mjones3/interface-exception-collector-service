package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.VerifyItemCommandInput;
import reactor.core.publisher.Mono;

public interface VerifyCartonService {

    Mono<UseCaseOutput<CartonOutput>> verifyCartonItem(VerifyItemCommandInput verifyItemCommandInput);

}
