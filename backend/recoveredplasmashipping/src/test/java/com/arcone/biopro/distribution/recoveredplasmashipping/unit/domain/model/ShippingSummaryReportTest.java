package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShippingSummaryReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.SystemProcessProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingSummaryReportTest {

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private SystemProcessPropertyRepository systemProcessPropertyRepository;

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository criteriaRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RecoveredPlasmaShipment recoveredPlasmaShipment;

    @Mock
    private Location location;

    private List<SystemProcessProperty> systemProperties;

    void setUp() {
        // Setup basic mocked data
        when(recoveredPlasmaShipment.getCloseEmployeeId()).thenReturn("EMP123");
        when(recoveredPlasmaShipment.getLocationCode()).thenReturn("LOC001");
        when(recoveredPlasmaShipment.getCloseDate()).thenReturn(ZonedDateTime.now());
        when(recoveredPlasmaShipment.getProductType()).thenReturn("PRODUCT_TYPE");
        when(recoveredPlasmaShipment.getShipmentNumber()).thenReturn("SHIPMENT_NUMBER");

        ShipmentCustomer shipmentCustomer = Mockito.mock(ShipmentCustomer.class, RETURNS_DEEP_STUBS);
        when(recoveredPlasmaShipment.getShipmentCustomer()).thenReturn(shipmentCustomer);

        // Mock system properties
        systemProperties = Arrays.asList(
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","USE_TRANSPORTATION_NUMBER", "Y"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","USE_HEADER_SECTION", "Y"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","DATE_FORMAT", "MM/dd/yyyy"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","DATE_TIME_FORMAT", "MM/dd/yyyy HH:mm z"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","TESTING_STATEMENT_TXT", "Test Statement"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","USE_TESTING_STATEMENT", "Y"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","ADDRESS_FORMAT", "{address} {city}, {state}, {zipCode} {country}"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","BLOOD_CENTER_NAME", "Testing"),
            createSystemProperty(1L,"RPS_SHIPPING_SUMMARY_REPORT","HEADER_SECTION_TXT", "HEADER")

        );


        when(systemProcessPropertyRepository.findAllByType("RPS_SHIPPING_SUMMARY_REPORT")).thenReturn(Flux.fromIterable(systemProperties));
        when(location.getTimeZone()).thenReturn("America/New_York");
        when(location.getCity()).thenReturn("CITY");
        when(location.getAddressLine1()).thenReturn("AddressLine1");
        when(location.getPostalCode()).thenReturn("portalCode");
        when(location.getState()).thenReturn("state");

        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));

        var productType = Mockito.mock(ProductType.class, RETURNS_DEEP_STUBS);
        when(productType.getProductTypeDescription()).thenReturn("PRODUCT_TYPE_DESCRIPTION");

        when(criteriaRepository.findBYProductType(Mockito.anyString())).thenReturn(Mono.just(productType));
    }

    @Test
    void generateReport_WithValidData_ShouldCreateReport() {
        // Arrange
        setUp();

        Carton carton = Mockito.mock(Carton.class,RETURNS_DEEP_STUBS);
        Mockito.when(carton.getCartonNumber()).thenReturn("CARTON_NUMBER");


        CartonItem cartonItem = Mockito.mock(CartonItem.class, RETURNS_DEEP_STUBS);
        Mockito.when(cartonItem.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(cartonItem.getProductDescription()).thenReturn("PRODUCT_DESCRIPTION");

        Mockito.when(carton.getProducts()).thenReturn(List.of(cartonItem));


        when(cartonRepository.findAllByShipment(Mockito.anyLong())).thenReturn(Flux.just(carton));

        // Act
        ShippingSummaryReport report = ShippingSummaryReport.generateReport(
            recoveredPlasmaShipment,
            cartonRepository,
            systemProcessPropertyRepository,
            criteriaRepository,
            locationRepository
        );

        // Assert
        assertNotNull(report);
        assertEquals("Plasma Shipment Summary Report", report.getReportTitle());
        assertEquals("EMP123", report.getEmployeeId());
        assertEquals("EMP123", report.getEmployeeName());
        assertTrue(report.isDisplayHeader());
        assertNotNull(report.getShipmentDetail());
        assertNotNull(report.getShipTo());
        assertNotNull(report.getShipFrom());
        assertEquals("Test Statement",report.getTestingStatement());
        assertEquals("HEADER",report.getHeaderStatement());
        assertNotNull(report.getCloseDate());
        assertNotNull(report.getCloseDateTime());
        assertEquals("AddressLine1 CITY, state, portalCode USA",report.getShipFrom().getLocationAddress());


    }

    private SystemProcessProperty createSystemProperty(Long id, String type , String key, String value) {
        return new SystemProcessProperty(id,type,key, value);
    }

    @Test
    void generateReport_WithNoCartons_ShouldCreateReportWithEmptyCartonList() {
        // Arrange
        setUp();
        when(cartonRepository.findAllByShipment(Mockito.anyLong())).thenReturn(Flux.empty());

        // Act
        ShippingSummaryReport report = ShippingSummaryReport.generateReport(
            recoveredPlasmaShipment,
            cartonRepository,
            systemProcessPropertyRepository,
            criteriaRepository,
            locationRepository
        );

        // Assert
        assertNotNull(report);
        assertTrue(report.getCartonList().isEmpty());
    }

    @Test
    void generateReport_WithNullLocation_ShouldThrowException() {
        // Arrange
        when(locationRepository.findOneByCode(Mockito.any())).thenReturn(Mono.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ShippingSummaryReport.generateReport(
                recoveredPlasmaShipment,
                cartonRepository,
                systemProcessPropertyRepository,
                criteriaRepository,
                locationRepository
            )
        );
    }
}

