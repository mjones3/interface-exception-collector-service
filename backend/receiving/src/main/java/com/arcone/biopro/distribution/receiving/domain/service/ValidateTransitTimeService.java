package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTemperatureCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTransitTimeCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateTransitTimeCommand;
import reactor.core.publisher.Mono;

public interface ValidateTransitTimeService {

    Mono<UseCaseOutput<ValidationResultOutput>> validateTransitTime(ValidateTransitTimeCommandInput validateTransitTimeCommandInput);
}
