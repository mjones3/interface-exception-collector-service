package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.FindShipmentRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.QueryOrderByDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentQueryCommandRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.FindShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.QueryOrderByOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.QuerySortOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CommandRequestDTOMapperTest {


    private CommandRequestDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CommandRequestDTOMapper.class);
    }

    @Test
    void shouldMapRecoveredPlasmaShipmentQueryCommandRequestDTO() {
        // Given
        QuerySortDTO querySortDTO = new QuerySortDTO(List.of(
            new QueryOrderByDTO("field1", "ASC"),
            new QueryOrderByDTO("field2", "DESC")
        ));

        RecoveredPlasmaShipmentQueryCommandRequestDTO requestDTO = RecoveredPlasmaShipmentQueryCommandRequestDTO
            .builder()
            .shipmentNumber("123")
            .locationCode(List.of("LOC123"))
            .customers(List.of("EMP456"))
            .querySort(querySortDTO)
            .build();


        // When
        RecoveredPlasmaShipmentQueryCommandInput result = mapper.toInputCommand(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals( List.of("LOC123"), result.locationCode());
        assertEquals(List.of("EMP456"), result.customers());

        assertNotNull(result.querySort());
        assertEquals(2, result.querySort().queryOrderByList().size());

        QueryOrderByOutput firstOrderBy = result.querySort().queryOrderByList().get(0);
        assertEquals("field1", firstOrderBy.property());
        assertEquals("ASC", firstOrderBy.direction());

        QueryOrderByOutput secondOrderBy = result.querySort().queryOrderByList().get(1);
        assertEquals("field2", secondOrderBy.property());
        assertEquals("DESC", secondOrderBy.direction());
    }

    @Test
    void shouldHandleNullQuerySortDTO() {
        // Given
        RecoveredPlasmaShipmentQueryCommandRequestDTO requestDTO = RecoveredPlasmaShipmentQueryCommandRequestDTO
            .builder()
            .shipmentNumber("123")
            .locationCode(List.of("LOC123"))
            .customers(List.of("EMP456"))
            .build();

        // When
        RecoveredPlasmaShipmentQueryCommandInput result = mapper.toInputCommand(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals( List.of("LOC123"), result.locationCode());
        assertEquals(List.of("EMP456"), result.customers());
        assertNull(result.querySort());
    }

    @Test
    void shouldHandleEmptyOrderByList() {
        // Given

        QuerySortDTO querySortDTO = new QuerySortDTO(Collections.emptyList());

        RecoveredPlasmaShipmentQueryCommandRequestDTO requestDTO = RecoveredPlasmaShipmentQueryCommandRequestDTO
            .builder()
            .shipmentNumber("123")
            .locationCode(List.of("LOC123"))
            .customers(List.of("EMP456"))
            .querySort(querySortDTO)
            .build();

        // When
        RecoveredPlasmaShipmentQueryCommandInput result = mapper.toInputCommand(requestDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.querySort());
        assertTrue(result.querySort().queryOrderByList().isEmpty());
    }

    @Test
    void shouldMapFindShipmentRequestDTO() {
        // Given
        FindShipmentRequestDTO requestDTO = FindShipmentRequestDTO
            .builder()
            .shipmentId(1L)
            .locationCode("LOC123")
            .employeeId("EMP456")
            .build();

        // When
        FindShipmentCommandInput result = mapper.toInputCommand(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.shipmentId());
        assertEquals("LOC123", result.locationCode());
        assertEquals("EMP456", result.employeeId());
    }

    @Test
    void shouldMapQuerySortDTOIndependently() {
        // Given
        QuerySortDTO querySortDTO = new QuerySortDTO(List.of(
            new QueryOrderByDTO("field1", "ASC"),
            new QueryOrderByDTO("field2", "DESC")
        ));


        // When
        QuerySortOutput result = mapper.toQuerySortOutput(querySortDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.queryOrderByList().size());

        QueryOrderByOutput firstOrderBy = result.queryOrderByList().get(0);
        assertEquals("field1", firstOrderBy.property());
        assertEquals("ASC", firstOrderBy.direction());

        QueryOrderByOutput secondOrderBy = result.queryOrderByList().get(1);
        assertEquals("field2", secondOrderBy.property());
        assertEquals("DESC", secondOrderBy.direction());
    }

    @Test
    void shouldHandleNullQuerySortDTOIndependently() {
        // When
        QuerySortOutput result = mapper.toQuerySortOutput(null);

        // Then
        assertNull(result);
    }
}

