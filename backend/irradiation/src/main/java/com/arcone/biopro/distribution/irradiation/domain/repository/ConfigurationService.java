package com.arcone.biopro.distribution.irradiation.domain.repository;

import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ConfigurationService {

    Flux<Configuration> readConfiguration(List<String> keys);

}
