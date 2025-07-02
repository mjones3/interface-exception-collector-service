package com.arcone.biopro.distribution.irradiation.infrastructure.service;

import com.arcone.biopro.distribution.irradiation.application.service.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.TemperatureCategoryEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.TemperatureCategoryEntityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigurationServiceImpl implements ConfigurationService {

    private final TemperatureCategoryEntityRepository temperatureCategoryEntityRepository;

    @Override
    public Mono<String> lookUpTemperatureCategory(String productCode) {
        return temperatureCategoryEntityRepository.findById(productCode).map(TemperatureCategoryEntity::getTemperatureCategory);
    }
}
