package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTemperatureCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import reactor.core.publisher.Mono;

public interface ValidateTemperatureService {

    Mono<UseCaseOutput<ValidationResultOutput>> validateTemperature(ValidateTemperatureCommandInput validateTemperatureCommandInput);
}
