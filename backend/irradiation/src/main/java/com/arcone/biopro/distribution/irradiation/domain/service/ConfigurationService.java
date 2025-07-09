package com.arcone.biopro.distribution.irradiation.domain.service;

import com.arcone.biopro.distribution.irradiation.domain.exception.ConfigurationNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConfigurationService {

    ConfigurationRepository configurationRepository;

    public Flux<Configuration> readConfiguration(List<String> keys) {
        return configurationRepository.readConfiguration(keys)
            .switchIfEmpty(Flux.error(ConfigurationNotFoundException::new));
    }
}
