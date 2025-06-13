package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonPackingSlip;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.SystemProcessProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartonPackingSlipTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SystemProcessPropertyRepository systemProcessPropertyRepository;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;

    @Test
    @DisplayName("Should generate packing slip successfully")
    void shouldGeneratePackingSlipSuccessfully() {
        // Given
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getId()).thenReturn(1L);
        Mockito.when(carton.getCartonSequence()).thenReturn(1);
        Mockito.when(carton.getShipmentId()).thenReturn(1L);
        Mockito.when(carton.getCartonNumber()).thenReturn("CARTON123");
        Mockito.when(carton.getCloseEmployeeId()).thenReturn("EMP123");
        Mockito.when(carton.getCloseDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(carton.getTotalProducts()).thenReturn(2);

        CartonItem cartonItem1 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem1.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(cartonItem1.getCollectionDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(cartonItem1.getVolume()).thenReturn(10);
        Mockito.when(cartonItem1.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(cartonItem1.getProductDescription()).thenReturn("PRODUCT_DESC");

        Mockito.when(carton.getVerifiedProducts()).thenReturn(List.of(cartonItem1));

        List<SystemProcessProperty> systemProperties = createMockSystemProperties();
        RecoveredPlasmaShipment shipment = createMockShipment();
        Location location = createMockLocation();

        // Mock repository calls
        when(systemProcessPropertyRepository.findAllByType("RPS_CARTON_PACKING_SLIP"))
            .thenReturn(Flux.fromIterable(systemProperties));
        when(recoveredPlasmaShippingRepository.findOneById(1L))
            .thenReturn(Mono.just(shipment));
        when(locationRepository.findOneByCode(anyString()))
            .thenReturn(Mono.just(location));
        when(recoveredPlasmaShipmentCriteriaRepository.findBYProductType("PLASMA"))
            .thenReturn(Mono.just(new ProductType(1,"PLASMA", "Recovered Plasma")));

        // When
        CartonPackingSlip packingSlip = CartonPackingSlip.generatePackingSlip(
            carton,
            locationRepository,
            systemProcessPropertyRepository,
            recoveredPlasmaShippingRepository,
            recoveredPlasmaShipmentCriteriaRepository
        );

        // Then
        assertNotNull(packingSlip);
        assertEquals(1L, packingSlip.getCartonId());
        assertEquals("CARTON123", packingSlip.getCartonNumber());
        assertEquals(1, packingSlip.getCartonSequence());
        assertEquals(2, packingSlip.getTotalProducts());
        assertNotNull(packingSlip.getDateTimePacked());
        assertEquals("EMP123", packingSlip.getPackedByEmployeeId());
        assertNotNull(packingSlip.getShipFrom());
        assertNotNull(packingSlip.getShipTo());
        assertNotNull(packingSlip.getPackingSlipShipment());
        assertTrue(packingSlip.isDisplaySignature());
        assertTrue(packingSlip.isDisplayTransportationReferenceNumber());
        assertTrue(packingSlip.isDisplayTestingStatement());
        assertTrue(packingSlip.isDisplayLicenceNumber());
        assertNotNull(packingSlip.getPackedProducts());
        assertEquals(1, packingSlip.getPackedProducts().size());
        assertEquals("UNIT_NUMBER", packingSlip.getPackedProducts().getFirst().getUnitNumber());
        assertNotNull( packingSlip.getPackedProducts().getFirst().getCollectionDateFormatted());
        assertEquals("PRODUCT_CODE", packingSlip.getCartonProductCode());
        assertEquals("PRODUCT_DESC", packingSlip.getCartonProductDescription());

        assertEquals("Products packed, inspected and found satisfactory by: EMP123", packingSlip.getTestingStatement());
    }

    @Test
    @DisplayName("Should throw exception when carton id is null")
    void shouldThrowExceptionWhenCartonIdIsNull() {
        // Given

        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getId()).thenReturn(null);
        Mockito.when(carton.getShipmentId()).thenReturn(1L);
        Mockito.when(carton.getCartonNumber()).thenReturn("TEST");
        Mockito.when(carton.getCloseEmployeeId()).thenReturn("closed-employeeid");
        Mockito.when(carton.getCloseDate()).thenReturn(ZonedDateTime.now());

        CartonItem cartonItem1 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem1.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(cartonItem1.getCollectionDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(cartonItem1.getVolume()).thenReturn(10);

        Mockito.when(carton.getVerifiedProducts()).thenReturn(List.of(cartonItem1));

        List<SystemProcessProperty> systemProperties = createMockSystemProperties();
        RecoveredPlasmaShipment shipment = createMockShipment();
        Location location = createMockLocation();

        // Mock repository calls
        when(systemProcessPropertyRepository.findAllByType("RPS_CARTON_PACKING_SLIP"))
            .thenReturn(Flux.fromIterable(systemProperties));
        when(recoveredPlasmaShippingRepository.findOneById(1L))
            .thenReturn(Mono.just(shipment));
        when(locationRepository.findOneByCode(anyString()))
            .thenReturn(Mono.just(location));
        when(recoveredPlasmaShipmentCriteriaRepository.findBYProductType("PLASMA"))
            .thenReturn(Mono.just(new ProductType(1,"PLASMA", "Recovered Plasma")));

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CartonPackingSlip.generatePackingSlip(
                carton,
                locationRepository,
                systemProcessPropertyRepository,
                recoveredPlasmaShippingRepository,
                recoveredPlasmaShipmentCriteriaRepository
            )
        );

        assertEquals("Carton id is required", exception.getMessage());
    }
    @Test
    @DisplayName("Should throw exception when carton number is null")
    void shouldThrowExceptionWhenCartonNumberIsNull() {
        // Given
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getShipmentId()).thenReturn(1L);
        Mockito.when(carton.getCloseDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(carton.getCloseEmployeeId()).thenReturn("test");

        CartonItem cartonItem1 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem1.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(cartonItem1.getCollectionDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(cartonItem1.getVolume()).thenReturn(10);

        Mockito.when(carton.getVerifiedProducts()).thenReturn(List.of(cartonItem1));

        List<SystemProcessProperty> systemProperties = createMockSystemProperties();
        RecoveredPlasmaShipment shipment = createMockShipment();
        Location location = createMockLocation();

        // Mock repository calls
        when(systemProcessPropertyRepository.findAllByType("RPS_CARTON_PACKING_SLIP"))
            .thenReturn(Flux.fromIterable(systemProperties));
        when(recoveredPlasmaShippingRepository.findOneById(1L))
            .thenReturn(Mono.just(shipment));
        when(locationRepository.findOneByCode(anyString()))
            .thenReturn(Mono.just(location));

        var productType = Mockito.mock(ProductType.class);
        Mockito.when(productType.getProductTypeDescription()).thenReturn("Recovered Plasma");
        when(recoveredPlasmaShipmentCriteriaRepository.findBYProductType("PLASMA"))
            .thenReturn(Mono.just(productType));

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CartonPackingSlip.generatePackingSlip(
                carton,
                locationRepository,
                systemProcessPropertyRepository,
                recoveredPlasmaShippingRepository,
                recoveredPlasmaShipmentCriteriaRepository
            )
        );

        assertEquals("Carton Number is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when system properties repository returns empty")
    void shouldThrowExceptionWhenSystemPropertiesRepositoryReturnsEmpty() {
        // Given

        Carton carton = Mockito.mock(Carton.class);

        // Mock repository calls
        when(systemProcessPropertyRepository.findAllByType("RPS_CARTON_PACKING_SLIP"))
            .thenReturn(Flux.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CartonPackingSlip.generatePackingSlip(
                carton,
                locationRepository,
                systemProcessPropertyRepository,
                recoveredPlasmaShippingRepository,
                recoveredPlasmaShipmentCriteriaRepository
            )
        );

        assertEquals("System Property is required: RPS_CARTON_PACKING_SLIP", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when location repository returns empty")
    void shouldThrowExceptionWhenLocationRepositoryReturnsEmpty() {
        // Given
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getShipmentId()).thenReturn(1L);
        List<SystemProcessProperty> systemProperties = createMockSystemProperties();


        // Mock repository calls
        when(systemProcessPropertyRepository.findAllByType("RPS_CARTON_PACKING_SLIP"))
            .thenReturn(Flux.fromIterable(systemProperties));
        when(recoveredPlasmaShippingRepository.findOneById(1L)).thenReturn(Mono.just(Mockito.mock(RecoveredPlasmaShipment.class)));
        when(locationRepository.findOneByCode(Mockito.any()))
            .thenReturn(Mono.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CartonPackingSlip.generatePackingSlip(
                carton,
                locationRepository,
                systemProcessPropertyRepository,
                recoveredPlasmaShippingRepository,
                recoveredPlasmaShipmentCriteriaRepository
            )
        );

        assertEquals("Location is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when shipment repository returns empty")
    void shouldThrowExceptionWhenShipmentRepositoryReturnsEmpty() {
        // Given
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getShipmentId()).thenReturn(1L);
        List<SystemProcessProperty> systemProperties = createMockSystemProperties();

        // Mock repository calls
        when(systemProcessPropertyRepository.findAllByType("RPS_CARTON_PACKING_SLIP"))
            .thenReturn(Flux.fromIterable(systemProperties));
        when(recoveredPlasmaShippingRepository.findOneById(1L))
            .thenReturn(Mono.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CartonPackingSlip.generatePackingSlip(
                carton,
                locationRepository,
                systemProcessPropertyRepository,
                recoveredPlasmaShippingRepository,
                recoveredPlasmaShipmentCriteriaRepository
            )
        );

        assertEquals("Shipment is required", exception.getMessage());
    }

    // Helper methods to create mock objects
    private Carton createMockCarton() {
        return Carton.fromRepository(
            1L, "CARTON123", 1L, 1, "EMP123", "EMP123",
            ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(),
            "CLOSED", BigDecimal.ZERO, BigDecimal.ZERO, createMockCartonItems(), 1, 10
        );
    }

    private List<CartonItem> createMockCartonItems() {
        List<CartonItem> items = new ArrayList<>();

        CartonItem cartonItem1 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem1.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(cartonItem1.getProductType()).thenReturn("PRODUCT_CODE");

        items.add(cartonItem1);
        return items;
    }

    private List<SystemProcessProperty> createMockSystemProperties() {
        List<SystemProcessProperty> properties = new ArrayList<>();
        properties.add(new SystemProcessProperty(1L, "RPS_CARTON_PACKING_SLIP", "DATE_FORMAT", "yyyy-MM-dd"));
        properties.add(new SystemProcessProperty(2L, "RPS_CARTON_PACKING_SLIP", "DATE_TIME_FORMAT", "yyyy-MM-dd HH:mm:ss"));
        properties.add(new SystemProcessProperty(3L, "RPS_CARTON_PACKING_SLIP", "ADDRESS_FORMAT", "STANDARD"));
        properties.add(new SystemProcessProperty(4L, "RPS_CARTON_PACKING_SLIP", "BLOOD_CENTER_NAME", "Test Blood Center"));
        properties.add(new SystemProcessProperty(5L, "RPS_CARTON_PACKING_SLIP", "USE_SIGNATURE", "Y"));
        properties.add(new SystemProcessProperty(6L, "RPS_CARTON_PACKING_SLIP", "USE_TRANSPORTATION_NUMBER", "Y"));
        properties.add(new SystemProcessProperty(7L, "RPS_CARTON_PACKING_SLIP", "USE_TESTING_STATEMENT", "Y"));
        properties.add(new SystemProcessProperty(8L, "RPS_CARTON_PACKING_SLIP", "USE_LICENSE_NUMBER", "Y"));
        properties.add(new SystemProcessProperty(9L, "RPS_CARTON_PACKING_SLIP", "USE_TESTING_STATEMENT", "Y"));
        properties.add(new SystemProcessProperty(10L, "RPS_CARTON_PACKING_SLIP", "TESTING_STATEMENT_TXT", "Products packed, inspected and found satisfactory by: {employeeName}"));
        return properties;
    }

    private RecoveredPlasmaShipment createMockShipment() {
        ShipmentCustomer customer = Mockito.mock(ShipmentCustomer.class);

        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(recoveredPlasmaShipment.getShipmentCustomer()).thenReturn(customer);
        Mockito.when(recoveredPlasmaShipment.getLocationCode()).thenReturn("LOCATION_CODE");
        Mockito.when(recoveredPlasmaShipment.getProductType()).thenReturn("PLASMA");

        return recoveredPlasmaShipment;


    }

    private Location createMockLocation() {
        Location location = Mockito.mock(Location.class);
        Mockito.when(location.getTimeZone()).thenReturn("America/New_York");
        return  location;
    }
}
