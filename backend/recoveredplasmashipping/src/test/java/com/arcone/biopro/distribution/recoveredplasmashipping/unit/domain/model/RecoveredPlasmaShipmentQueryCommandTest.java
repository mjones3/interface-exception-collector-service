package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QuerySort;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecoveredPlasmaShipmentQueryCommandTest {

    @Test
    @DisplayName("Should create command with valid parameters")
    void shouldCreateCommandWithValidParameters() {
        // Given
        List<String> locationCode = List.of("LOC1");
        String shipmentNumber = "SHIP001";
        List<String> shipmentStatus = List.of("OPEN");
        List<String> customers = List.of("CUST1");
        List<String> productTypes = List.of("TYPE1");
        QuerySort querySort = new QuerySort(List.of(new QueryOrderBy("status", "DESC")));
        Integer pageNumber = 1;
        Integer pageSize = 10;

        // When
        RecoveredPlasmaShipmentQueryCommand command = new RecoveredPlasmaShipmentQueryCommand(
            locationCode, shipmentNumber, shipmentStatus, customers, productTypes,
            null, null, querySort, pageNumber, pageSize, null
        );

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getLocationCode()).isEqualTo(locationCode);
        assertThat(command.getShipmentNumber()).isEqualTo(shipmentNumber);
    }


    @Test
    @DisplayName("Should throw exception when shipment number and dates are provided together")
    void shouldThrowExceptionWhenShipmentNumberAndDatesProvided() {
        assertThatThrownBy(() -> new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), "SHIP001", List.of("OPEN"), List.of("CUST1"),
            List.of("TYPE1"), LocalDate.now(), LocalDate.now(),
            null, 1, 10, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The shipmentDate must be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when dates are invalid")
    void shouldThrowExceptionWhenDatesAreInvalid() {
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), null, List.of("OPEN"), List.of("CUST1"),
            List.of("TYPE1"), dateFrom, dateTo,
            null, 1, 10, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Initial date should not be greater than final date");
    }

    @Test
    @DisplayName("Should throw exception when date range exceeds two years")
    void shouldThrowExceptionWhenDateRangeExceedsTwoYears() {
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();

        assertThatThrownBy(() -> new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), null, List.of("OPEN"), List.of("CUST1"),
            List.of("TYPE1"), dateFrom, dateTo,
            null, 1, 10, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Shipment date range exceeds 2 years");
    }

    @Test
    @DisplayName("Should use default values when optional parameters are null")
    void shouldUseDefaultValuesWhenOptionalParametersAreNull() {
        // Given
        List<String> locationCode = List.of("LOC1");

        // When
        RecoveredPlasmaShipmentQueryCommand command = new RecoveredPlasmaShipmentQueryCommand(
            locationCode, null, null, null, null,
            null, null, null, null, null, null
        );

        // Then
        assertThat(command.getShipmentStatus()).isEqualTo(List.of("OPEN","IN_PROGRESS"));
        assertThat(command.getPageNumber()).isEqualTo(0);
        assertThat(command.getPageSize()).isEqualTo(20);
        assertThat(command.getQuerySort()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when page size is invalid")
    void shouldThrowExceptionWhenPageSizeIsInvalid() {
        assertThatThrownBy(() -> new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), null, List.of("OPEN","IN_PROGRESS"), null, null,
            null, null, null, 0, 0, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("pageSize must be greater than 0");
    }

    @Test
    @DisplayName("Should throw exception when page number is invalid")
    void shouldThrowExceptionWhenPageNumberIsInvalid() {
        assertThatThrownBy(() -> new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), null, List.of("OPEN","IN_PROGRESS"), null, null,
            null, null, null, -1, 10, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("pageNumber must be greater than or equal to 0");
    }

    @Test
    @DisplayName("Should throw exception when location is null or empty")
    void shouldThrowExceptionWhenLocationIsNull() {
        assertThatThrownBy(() -> new RecoveredPlasmaShipmentQueryCommand(
            null, null, List.of("OPEN"), null, null,
            null, null, null, -1, 10, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The locationCode must not be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when no parameters are provided other than date from and date to")
    void shouldThrowExceptionWhenDateFromIsNull() {
        // Given
        List<String> locationCode = List.of("LOC1");
        String shipmentNumber = null;
        List<String> shipmentStatus = List.of("OPEN","CLOSED");
        List<String> customers = null;
        List<String> productTypes = null;
        LocalDate dateFrom = LocalDate.now().minusDays(5);
        LocalDate dateTo = LocalDate.now();
        QuerySort querySort = new QuerySort(List.of(new QueryOrderBy("productType", "DESC")));
        Integer pageNumber = 1;
        Integer pageSize = 10;

        // When

        assertThatThrownBy(() -> new RecoveredPlasmaShipmentQueryCommand(
            locationCode, shipmentNumber, shipmentStatus, customers, productTypes,
            null, null, querySort, pageNumber, pageSize, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The shipmentDate must not be null or empty");
    }
}
