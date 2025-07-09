package com.arcone.biopro.distribution.irradiation.infrastructure.persistence;

import com.arcone.biopro.distribution.irradiation.domain.exception.ConfigurationNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.infrastructure.mapper.ConfigurationEntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;


@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    ConfigurationEntityRepository configurationEntityRepository;
    ConfigurationEntityMapper configurationEntityMapper;

    @Override
    public Flux<Configuration> readConfiguration(List<String> keys) {
        log.info("Loading irradiation configurations");
        return configurationEntityRepository
            .findByKeyIn(keys)
            .switchIfEmpty(Flux.error(ConfigurationNotFoundException::new))
            .map(configurationEntityMapper::toDomain);
    }

}
