package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QuerySort;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment.SHIPMENT_DATE_RANGE_YEARS_LIMIT;
import static com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand.*;
import static org.assertj.core.api.Assertions.assertThat;

class RecoveredPlasmaShipmentQueryCommandTest {

    @Test
    void shouldCreateCommandWithAllParametersExceptLocationCodes() {
        var command = new RecoveredPlasmaShipmentQueryCommand(null, "100", List.of("OPEN", "CLOSED", "IN_PROGRESS"), List.of("CUSTOMER 1", "CUSTOMER 2"), List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))), 10, 50, "500");
        assertThat(command.getLocationCode()).isNull();
        assertThat(command.getShipmentNumber()).isEqualTo("100");
        assertThat(command.getShipmentStatus()).isNull(); // shipmentStatus is not set when using shipmentNumber
        assertThat(command.getCustomers()).isEqualTo(List.of("CUSTOMER 1", "CUSTOMER 2"));
        assertThat(command.getProductTypes()).isEqualTo(List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"));
        assertThat(command.getShipmentDateFrom()).isNull(); // shipmentDateFrom is not set when using shipmentNumber
        assertThat(command.getShipmentDateTo()).isNull(); // shipmentDateTo is not set when using shipmentNumber
        assertThat(command.getQuerySort()).isEqualTo(new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))));
        assertThat(command.getPageNumber()).isEqualTo(10);
        assertThat(command.getPageSize()).isEqualTo(50);
        assertThat(command.getTransportationReferenceNumber()).isEqualTo("500");
    }

    @Test
    void shouldCreateCommandWithAllParametersExceptShipmentNumber() {
        var command = new RecoveredPlasmaShipmentQueryCommand(List.of("LOCATION 1", "LOCATION 2"), null, List.of("OPEN", "CLOSED", "IN_PROGRESS"), List.of("CUSTOMER 1", "CUSTOMER 2"), List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))), 10, 50, "500");
        assertThat(command.getLocationCode()).isEqualTo(List.of("LOCATION 1", "LOCATION 2"));
        assertThat(command.getShipmentNumber()).isNull();
        assertThat(command.getShipmentStatus()).isEqualTo(List.of("OPEN", "CLOSED", "IN_PROGRESS"));
        assertThat(command.getCustomers()).isEqualTo(List.of("CUSTOMER 1", "CUSTOMER 2"));
        assertThat(command.getProductTypes()).isEqualTo(List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"));
        assertThat(command.getShipmentDateFrom()).isEqualTo(LocalDate.now().minusDays(5));
        assertThat(command.getShipmentDateTo()).isEqualTo(LocalDate.now().plusDays(5));
        assertThat(command.getQuerySort()).isEqualTo(new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))));
        assertThat(command.getPageNumber()).isEqualTo(10);
        assertThat(command.getPageSize()).isEqualTo(50);
        assertThat(command.getTransportationReferenceNumber()).isEqualTo("500");
    }

    @Test
    void shouldCreateCommandWithOnlyLocation() {
        var command = new RecoveredPlasmaShipmentQueryCommand(List.of("LOCATION"), null, null, null, null, null, null, null, null, null, null);
        assertThat(command.getLocationCode()).isEqualTo(List.of("LOCATION"));
        assertThat(command.getShipmentNumber()).isNull();
        assertThat(command.getShipmentStatus()).isEqualTo(DEFAULT_STATUSES);
        assertThat(command.getCustomers()).isNull();
        assertThat(command.getProductTypes()).isNull();
        assertThat(command.getShipmentDateFrom()).isEqualTo(LocalDate.now().minusYears(SHIPMENT_DATE_RANGE_YEARS_LIMIT));
        assertThat(command.getShipmentDateTo()).isNull();
        assertThat(command.getQuerySort()).isEqualTo(new QuerySort(DEFAULT_SORTING));
        assertThat(command.getPageNumber()).isEqualTo(DEFAULT_PAGE_NUMBER_FIRST_PAGE);
        assertThat(command.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(command.getTransportationReferenceNumber()).isNull();
    }

    @Test
    void shouldCreateCommandWithOnlyShipmentNumber() {
        var command = new RecoveredPlasmaShipmentQueryCommand(null, "1", null, null, null, null, null, null, null, null, null);
        assertThat(command.getLocationCode()).isNull();
        assertThat(command.getShipmentNumber()).isEqualTo("1");
        assertThat(command.getShipmentStatus()).isNull();
        assertThat(command.getCustomers()).isNull();
        assertThat(command.getProductTypes()).isNull();
        assertThat(command.getShipmentDateFrom()).isNull();
        assertThat(command.getShipmentDateTo()).isNull();
        assertThat(command.getQuerySort()).isEqualTo(new QuerySort(DEFAULT_SORTING));
        assertThat(command.getPageNumber()).isEqualTo(DEFAULT_PAGE_NUMBER_FIRST_PAGE);
        assertThat(command.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(command.getTransportationReferenceNumber()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenWithoutShipmentNumberAndLocationCode() {
        var exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> new RecoveredPlasmaShipmentQueryCommand(null, null, List.of("OPEN", "CLOSED", "IN_PROGRESS"), List.of("CUSTOMER 1", "CUSTOMER 2"), List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))), 10, 50, "500"));

        assertThat(exception.getMessage()).isEqualTo("The locationCode must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenShipmentDateRangeIsWrong() {
        var shipmentDateFrom = LocalDate.now().plusDays(5);
        var shipmentDateTo = LocalDate.now(); // The shipmentDateTo is BEFORE shipmentDateFrom and it should not

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> new RecoveredPlasmaShipmentQueryCommand(List.of("LOCATION"), null, List.of("OPEN", "CLOSED", "IN_PROGRESS"), List.of("CUSTOMER 1", "CUSTOMER 2"), List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"), shipmentDateFrom, shipmentDateTo, new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))), 10, 50, "500"));

        assertThat(exception.getMessage()).isEqualTo("Initial date should not be greater than final date");
    }

    @Test
    void shouldThrowExceptionWhenShipmentDateRangeExceedsLimit() {
        var shipmentDateFrom = LocalDate.now();
        var shipmentDateTo = LocalDate.now()
            .plusYears(SHIPMENT_DATE_RANGE_YEARS_LIMIT)
            .plusDays(1); // Adding one day exceeding the limit

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> new RecoveredPlasmaShipmentQueryCommand(List.of("LOCATION"), null, List.of("OPEN", "CLOSED", "IN_PROGRESS"), List.of("CUSTOMER 1", "CUSTOMER 2"), List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"), shipmentDateFrom, shipmentDateTo, new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))), 10, 50, "500"));

        assertThat(exception.getMessage()).isEqualTo("Shipment date range exceeds " + SHIPMENT_DATE_RANGE_YEARS_LIMIT + " years");
    }

    @Test
    void shouldThrowExceptionWhenPageSizeIsLesserThan1() {
        var exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> new RecoveredPlasmaShipmentQueryCommand(List.of("LOCATION"), null, List.of("OPEN", "CLOSED", "IN_PROGRESS"), List.of("CUSTOMER 1", "CUSTOMER 2"), List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))), 10, -1, "500"));

        assertThat(exception.getMessage()).isEqualTo("pageSize must be greater than 0");
    }

    @Test
    void shouldThrowExceptionWhenPageNumberIsLesserThanZero() {
        var exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> new RecoveredPlasmaShipmentQueryCommand(List.of("LOCATION"), null, List.of("OPEN", "CLOSED", "IN_PROGRESS"), List.of("CUSTOMER 1", "CUSTOMER 2"), List.of("PRODUCT TYPE 1", "PRODUCT TYPE 2"), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), new QuerySort(List.of(new QueryOrderBy("PROP", "DESC"))), -1, 50, "500"));

        assertThat(exception.getMessage()).isEqualTo("pageNumber must be greater than or equal to 0");
    }

}
