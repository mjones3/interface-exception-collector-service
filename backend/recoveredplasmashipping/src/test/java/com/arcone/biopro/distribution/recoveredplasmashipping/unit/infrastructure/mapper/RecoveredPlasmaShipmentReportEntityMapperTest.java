package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentReportEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentReportEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveredPlasmaShipmentReportEntityMapperTest {

    private RecoveredPlasmaShipmentReportEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RecoveredPlasmaShipmentReportEntityMapper.class);
    }

    @Test
    void shouldMapEntityToModel() {
        // Given
        RecoveredPlasmaShipmentReportEntity entity = RecoveredPlasmaShipmentReportEntity
            .builder()
            .id(1L)
            .shipmentNumber("SHIP001")
            .customerCode("customerCode")
            .shipmentDate(ZonedDateTime.now())
            .customerName("customerName")
            .locationCode("locationCOde")
            .location("location")
            .productType("productType")
            .productTypeDescription("productTypeDescription")
            .scheduleDate(LocalDate.now())
            .status("OPEN")
            .build();

        // When
        RecoveredPlasmaShipmentReport result = mapper.toModel(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getShipmentId()).isEqualTo(entity.getId());
        assertThat(result.getShipmentNumber()).isEqualTo(entity.getShipmentNumber());
        assertThat(result.getCustomerName()).isEqualTo(entity.getCustomerName());
        assertThat(result.getShipmentDate()).isNotNull();
        assertThat(result.getCustomerName()).isEqualTo(entity.getCustomerName());
        assertThat(result.getLocation()).isEqualTo(entity.getLocation());
        assertThat(result.getProductType()).isEqualTo(entity.getProductTypeDescription());
        assertThat(result.getStatus()).isEqualTo(entity.getStatus());


        // Add other assertions for remaining properties
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        // Given
        RecoveredPlasmaShipmentReportEntity entity = null;

        // When
        RecoveredPlasmaShipmentReport result = mapper.toModel(entity);

        // Then
        assertThat(result).isNull();
    }
}

