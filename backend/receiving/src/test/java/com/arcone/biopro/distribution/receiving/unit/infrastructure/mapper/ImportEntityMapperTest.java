package com.arcone.biopro.distribution.receiving.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ImportEntityMapper;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.ImportEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class ImportEntityMapperTest {

    private final ImportEntityMapper mapper = Mappers.getMapper(ImportEntityMapper.class);

    @Test
    void mapToDomain_ValidEntity_ReturnsDomainObject() {
        // Arrange
        ImportEntity entity = createTestEntity();

        // Act
        Import result = mapper.mapToDomain(entity,10);

        // Assert
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getTemperatureCategory(), result.getTemperatureCategory());
        assertEquals(entity.getTransitStartDateTime(), result.getTransitStartDateTime());
        assertEquals(entity.getTransitStartTimeZone(), result.getTransitStartTimeZone());
        assertEquals(entity.getTransitEndDateTime(), result.getTransitEndDateTime());
        assertEquals(entity.getTransitEndTimeZone(), result.getTransitEndTimeZone());
        assertEquals(entity.getTotalTransitTime(), result.getTotalTransitTime());
        assertEquals(entity.getTransitTimeResult(), result.getTransitTimeResult());
        assertEquals(entity.getTemperature(), result.getTemperature());
        assertEquals(entity.getThermometerCode(), result.getThermometerCode());
        assertEquals(entity.getTemperatureResult(), result.getTemperatureResult());
        assertEquals(entity.getLocationCode(), result.getLocationCode());
        assertEquals(entity.getComments(), result.getComments());
        assertEquals(entity.getStatus(), result.getStatus());
        assertEquals(entity.getEmployeeId(), result.getEmployeeId());
        assertEquals(entity.getCreateDate(), result.getCreateDate());
        assertEquals(entity.getModificationDate(), result.getModificationDate());
        assertEquals(10, result.getMaxNumberOfProducts());
    }

    @Test
    void toEntity_ValidDomain_ReturnsEntity() {
        // Arrange
        Import domain = createTestDomain();

        // Act
        ImportEntity result = mapper.toEntity(domain);

        // Assert
        assertNotNull(result);
        assertEquals(domain.getId(), result.getId());
        assertEquals(domain.getTemperatureCategory(), result.getTemperatureCategory());
        assertEquals(domain.getTransitStartDateTime(), result.getTransitStartDateTime());
        assertEquals(domain.getTransitStartTimeZone(), result.getTransitStartTimeZone());
        assertEquals(domain.getTransitEndDateTime(), result.getTransitEndDateTime());
        assertEquals(domain.getTransitEndTimeZone(), result.getTransitEndTimeZone());
        assertEquals(domain.getTotalTransitTime(), result.getTotalTransitTime());
        assertEquals(domain.getTransitTimeResult(), result.getTransitTimeResult());
        assertEquals(domain.getTemperature(), result.getTemperature());
        assertEquals(domain.getThermometerCode(), result.getThermometerCode());
        assertEquals(domain.getTemperatureResult(), result.getTemperatureResult());
        assertEquals(domain.getLocationCode(), result.getLocationCode());
        assertEquals(domain.getComments(), result.getComments());
        assertEquals(domain.getStatus(), result.getStatus());
        assertEquals(domain.getEmployeeId(), result.getEmployeeId());
        assertEquals(domain.getCreateDate(), result.getCreateDate());
        assertEquals(domain.getModificationDate(), result.getModificationDate());
    }

    @Test
    void toEntity_NullDomain_ReturnsNull() {
        // Act
        ImportEntity result = mapper.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void bidirectionalMapping_MaintainsDataIntegrity() {
        // Arrange
        Import originalDomain = createTestDomain();

        // Act
        ImportEntity entity = mapper.toEntity(originalDomain);
        Import mappedBackDomain = mapper.mapToDomain(entity,10);

        // Assert
        assertEquals(originalDomain.getId(), mappedBackDomain.getId());
        assertEquals(originalDomain.getTemperatureCategory(), mappedBackDomain.getTemperatureCategory());
        assertEquals(originalDomain.getTransitStartDateTime(), mappedBackDomain.getTransitStartDateTime());
        assertEquals(originalDomain.getTemperature(), mappedBackDomain.getTemperature());
        // ... add other relevant field comparisons
    }

    private ImportEntity createTestEntity() {
        return ImportEntity.builder()
            .id(1L)
            .temperatureCategory("FROZEN")
            .transitStartDateTime(LocalDateTime.now())
            .transitStartTimeZone("UTC")
            .transitEndDateTime(LocalDateTime.now().plusHours(2))
            .transitEndTimeZone("UTC")
            .totalTransitTime("2")
            .transitTimeResult("ACCEPTABLE")
            .temperature(BigDecimal.valueOf(20.5))
            .thermometerCode("THERM123")
            .temperatureResult("ACCEPTABLE")
            .locationCode("LOC123")
            .comments("Test comment")
            .status("PENDING")
            .employeeId("EMP123")
            .createDate(ZonedDateTime.now())
            .modificationDate(ZonedDateTime.now()).build();
    }

    private Import createTestDomain() {
    return Import.fromRepository(1L,
            "FROZEN",
            LocalDateTime.now(),
            "UTC",
            LocalDateTime.now().plusHours(2),
            "UTC",
            "2",
            "ACCEPTABLE",
            BigDecimal.valueOf(20.5),
            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            ZonedDateTime.now(),
            ZonedDateTime.now(),null,10);
    }
}

