package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.SystemProcessProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.UnacceptableUnitReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class UnacceptableUnitReportTest {

    @Mock
    private UnacceptableUnitReportRepository unacceptableUnitReportRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SystemProcessPropertyRepository systemProcessPropertyRepository;

    @Mock
    private RecoveredPlasmaShipment recoveredPlasmaShipment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReport_WithValidInputs_ShouldCreateReport() {
        // Arrange
        String locationCode = "LOC123";
        String shipmentNumber = "SHIP123";
        Long shipmentId = 1L;

        Location location = Mockito.mock(Location.class);
        Mockito.when(location.getTimeZone()).thenReturn("America/New_York");

        List<SystemProcessProperty> systemProperties = Arrays.asList(
            new  SystemProcessProperty(1L,"RPS_UNACCEPTABLE_UNITS_REPORT","DATE_TIME_FORMAT","yyyy-MM-dd HH:mm:ss")
        );

        UnacceptableUnitReportItem item = Mockito.mock(UnacceptableUnitReportItem.class);
        List<UnacceptableUnitReportItem> reportItems = Arrays.asList(item);

        when(recoveredPlasmaShipment.getShipmentNumber()).thenReturn(shipmentNumber);
        when(recoveredPlasmaShipment.getId()).thenReturn(shipmentId);
        when(locationRepository.findOneByCode(locationCode)).thenReturn(Mono.just(location));
        when(systemProcessPropertyRepository.findAllByType("RPS_UNACCEPTABLE_UNITS_REPORT"))
            .thenReturn(Flux.fromIterable(systemProperties));
        when(unacceptableUnitReportRepository.findAllByShipmentId(shipmentId))
            .thenReturn(Flux.fromIterable(reportItems));

        // Act
        UnacceptableUnitReport report = UnacceptableUnitReport.createReport(
            recoveredPlasmaShipment,
            unacceptableUnitReportRepository,
            locationRepository,
            systemProcessPropertyRepository,
            locationCode
        );

        // Assert
        assertNotNull(report);
        assertEquals(shipmentNumber, report.getShipmentNumber());
        assertNotNull(report.getDateTimeExported());
        assertEquals(reportItems, report.getFailedProducts());
    }

    @Test
    void createReport_WithNullLocationRepository_ShouldThrowException() {
        // Arrange
        String locationCode = "LOC123";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UnacceptableUnitReport.createReport(
                recoveredPlasmaShipment,
                unacceptableUnitReportRepository,
                null,
                systemProcessPropertyRepository,
                locationCode
            )
        );
        assertEquals("LocationRepository is required", exception.getMessage());
    }

    @Test
    void createReport_WithInvalidLocation_ShouldThrowException() {
        // Arrange
        String locationCode = "INVALID_LOC";
        when(locationRepository.findOneByCode(locationCode)).thenReturn(Mono.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UnacceptableUnitReport.createReport(
                recoveredPlasmaShipment,
                unacceptableUnitReportRepository,
                locationRepository,
                systemProcessPropertyRepository,
                locationCode
            )
        );
        assertEquals("Location is required", exception.getMessage());
    }
}

