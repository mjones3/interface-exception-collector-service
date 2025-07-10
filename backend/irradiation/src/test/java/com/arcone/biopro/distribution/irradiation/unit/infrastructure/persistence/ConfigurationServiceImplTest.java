package com.arcone.biopro.distribution.irradiation.unit.infrastructure.persistence;

import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ConfigurationKey;
import com.arcone.biopro.distribution.irradiation.infrastructure.mapper.ConfigurationEntityMapper;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.ConfigurationEntityRepository;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.ConfigurationServiceImpl;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.entity.ConfigurationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceImplTest {

    @Mock
    private ConfigurationEntityRepository configurationEntityRepository;

    @Mock
    private ConfigurationEntityMapper configurationEntityMapper;

    @InjectMocks
    private ConfigurationServiceImpl configurationService;

    @Test
    @DisplayName("Should return configurations when found")
    void readConfiguration_Success() {
        ConfigurationEntity entity = ConfigurationEntity.builder()
                .key("test.key")
                .value("test.value")
                .build();

        Configuration config = Configuration.builder()
                .key(new ConfigurationKey("test.key"))
                .value("test.value")
                .build();

        when(configurationEntityRepository.findByKeyIn(anyList()))
                .thenReturn(Flux.just(entity));
        when(configurationEntityMapper.toDomain(any(ConfigurationEntity.class)))
                .thenReturn(config);

        Flux<Configuration> result = configurationService.readConfiguration(List.of("test.key"));

        StepVerifier.create(result)
                .expectNext(config)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle multiple configurations")
    void readConfiguration_Multiple() {
        ConfigurationEntity entity1 = ConfigurationEntity.builder()
                .key("key1")
                .value("value1")
                .build();

        ConfigurationEntity entity2 = ConfigurationEntity.builder()
                .key("key2")
                .value("value2")
                .build();

        Configuration config1 = Configuration.builder()
                .key(new ConfigurationKey("key1"))
                .value("value1")
                .build();

        Configuration config2 = Configuration.builder()
                .key(new ConfigurationKey("key2"))
                .value("value2")
                .build();

        when(configurationEntityRepository.findByKeyIn(anyList()))
                .thenReturn(Flux.just(entity1, entity2));
        when(configurationEntityMapper.toDomain(entity1))
                .thenReturn(config1);
        when(configurationEntityMapper.toDomain(entity2))
                .thenReturn(config2);

        Flux<Configuration> result = configurationService.readConfiguration(List.of("key1", "key2"));

        StepVerifier.create(result)
                .expectNext(config1)
                .expectNext(config2)
                .verifyComplete();
    }
}
