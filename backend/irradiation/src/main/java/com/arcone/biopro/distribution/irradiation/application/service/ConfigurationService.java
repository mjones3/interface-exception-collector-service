package com.arcone.biopro.distribution.irradiation.application.service;

import reactor.core.publisher.Mono;

public interface ConfigurationService {

    Mono<String> lookUpTemperatureCategory(String productCode);

}
