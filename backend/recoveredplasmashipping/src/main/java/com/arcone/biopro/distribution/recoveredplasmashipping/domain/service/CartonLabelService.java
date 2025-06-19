package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.GenerateCartonLabelCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LabelOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface CartonLabelService {

    Mono<UseCaseOutput<LabelOutput>> generateCartonLabel(GenerateCartonLabelCommandInput generateCartonLabelCommandInput);
}
