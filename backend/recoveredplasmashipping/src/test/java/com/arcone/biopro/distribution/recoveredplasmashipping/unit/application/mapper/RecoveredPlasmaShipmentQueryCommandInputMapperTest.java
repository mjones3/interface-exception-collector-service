package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.QueryOrderByOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.QuerySortOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentQueryCommandInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveredPlasmaShipmentQueryCommandInputMapperTest {

    private final RecoveredPlasmaShipmentQueryCommandInputMapper mapper =
        Mappers.getMapper(RecoveredPlasmaShipmentQueryCommandInputMapper.class);

    @Test
    void shouldMapAllFieldsCorrectly() {
        // Given
        RecoveredPlasmaShipmentQueryCommandInput input = RecoveredPlasmaShipmentQueryCommandInput.builder()
            .pageNumber(0)
            .pageSize(10)
            .querySort(QuerySortOutput.builder().queryOrderByList(List.of(QueryOrderByOutput
                    .builder()
                    .property("shipmentDate")
                    .direction("DESC")
                    .build()))
                .build())
            .shipmentNumber(null)
            .locationCode(List.of("FAC-001"))
            .customers(List.of("FAC-002"))
            .shipmentDateFrom(LocalDate.now())
            .shipmentDateTo(LocalDate.now())
            .shipmentStatus(List.of("PENDING"))
            .productTypes(List.of("RECOVERED"))
            .build();

        // When
        RecoveredPlasmaShipmentQueryCommand result = mapper.toModel(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(input.pageNumber());
        assertThat(result.getPageSize()).isEqualTo(input.pageSize());
        assertThat(result.getQuerySort()).isNotNull();
        assertThat(result.getQuerySort().getQueryOrderByList()).hasSize(1);
        assertThat(result.getQuerySort().getQueryOrderByList().get(0).getProperty()).isEqualTo("shipmentDate");
        assertThat(result.getQuerySort().getQueryOrderByList().get(0).getDirection()).isEqualTo("DESC");
        assertThat(result.getShipmentNumber()).isEqualTo(input.shipmentNumber());
        assertThat(result.getLocationCode()).isEqualTo(input.locationCode());
        assertThat(result.getCustomers()).isEqualTo(input.customers());
        assertThat(result.getShipmentDateFrom()).isEqualTo(input.shipmentDateFrom());
        assertThat(result.getShipmentDateTo()).isEqualTo(input.shipmentDateTo());
        assertThat(result.getShipmentStatus()).isEqualTo(input.shipmentStatus());
        assertThat(result.getProductTypes()).isEqualTo(input.productTypes());

    }

    @Test
    void shouldMapAllFieldsCorrectlyWhenShipmentNumber() {
        // Given
        RecoveredPlasmaShipmentQueryCommandInput input = RecoveredPlasmaShipmentQueryCommandInput.builder()
            .pageNumber(0)
            .pageSize(10)
            .querySort(QuerySortOutput.builder().queryOrderByList(List.of(QueryOrderByOutput
                    .builder()
                    .property("shipmentDate")
                    .direction("DESC")
                    .build()))
                .build())
            .shipmentNumber("123")
            .locationCode(List.of("FAC-001"))
            .customers(List.of("FAC-002"))
            .shipmentStatus(List.of("PENDING"))
            .productTypes(List.of("RECOVERED"))
            .build();

        // When
        RecoveredPlasmaShipmentQueryCommand result = mapper.toModel(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(input.pageNumber());
        assertThat(result.getPageSize()).isEqualTo(input.pageSize());
        assertThat(result.getQuerySort()).isNotNull();
        assertThat(result.getQuerySort().getQueryOrderByList()).hasSize(1);
        assertThat(result.getQuerySort().getQueryOrderByList().get(0).getProperty()).isEqualTo("shipmentDate");
        assertThat(result.getQuerySort().getQueryOrderByList().get(0).getDirection()).isEqualTo("DESC");
        assertThat(result.getShipmentNumber()).isEqualTo(input.shipmentNumber());
        assertThat(result.getLocationCode()).isEqualTo(input.locationCode());
        assertThat(result.getCustomers()).isEqualTo(input.customers());
        assertThat(result.getShipmentDateFrom()).isEqualTo(input.shipmentDateFrom());
        assertThat(result.getShipmentDateTo()).isEqualTo(input.shipmentDateTo());
        assertThat(result.getShipmentStatus()).isEqualTo(input.shipmentStatus());
        assertThat(result.getProductTypes()).isEqualTo(input.productTypes());

    }

}
