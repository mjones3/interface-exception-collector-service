package com.arcone.biopro.distribution.inventory.application.service;

import reactor.core.publisher.Mono;

public interface ConfigurationService {

    Mono<String> lookUpTemperatureCategory(String productCode);

}
