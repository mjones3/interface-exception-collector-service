package com.arcone.biopro.distribution.irradiation.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.infrastructure.mapper.ConfigurationEntityMapper;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.entity.ConfigurationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationEntityMapperTest {

    private final ConfigurationEntityMapper mapper = Mappers.getMapper(ConfigurationEntityMapper.class);

    @Test
    @DisplayName("Should map ConfigurationEntity to Configuration domain model")
    void toDomain_Success() {
        ConfigurationEntity entity = ConfigurationEntity.builder()
                .key("test.key")
                .value("test.value")
                .active(true)
                .createDate(LocalDateTime.now())
                .modificationDate(LocalDateTime.now())
                .build();

        Configuration result = mapper.toDomain(entity);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertEquals("test.key", result.getKey().value());
        assertEquals("test.value", result.getValue());
    }

    @Test
    @DisplayName("Should handle null values in mapping")
    void toDomain_NullValues() {
        ConfigurationEntity entity = ConfigurationEntity.builder()
                .key(null)
                .value(null)
                .build();

        Configuration result = mapper.toDomain(entity);

        assertNotNull(result);
    }
}
