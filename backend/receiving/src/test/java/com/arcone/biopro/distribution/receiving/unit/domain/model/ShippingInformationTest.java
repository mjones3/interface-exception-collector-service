package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.EnterShippingInformationCommand;
import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import com.arcone.biopro.distribution.receiving.domain.model.Location;
import com.arcone.biopro.distribution.receiving.domain.model.Lookup;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.ShippingInformation;
import com.arcone.biopro.distribution.receiving.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingInformationTest {

    @Mock
    private LookupRepository lookupRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Test
    void shouldCreateShippingInformationSuccessfully() {
        // Given
        String productCategory = "ELECTRONICS";
        EnterShippingInformationCommand command = new EnterShippingInformationCommand(
            productCategory,
            "EMP123",
            "LOC456"
        );



        List<Lookup> transitTimeZones = Arrays.asList(
            Lookup.fromRepository(1L,"TIME_ZONE","TZ1","Transit Zone 1", 1,true),
            Lookup.fromRepository(2L,"TIME_ZONE","TZ2","Transit Zone 2", 1,true)

        );

        List<Lookup> visualInspections = Arrays.asList(
            Lookup.fromRepository(1L,"TIME_ZONE","VI1","Visual Inspection 1", 1,true),
            Lookup.fromRepository(1L,"TIME_ZONE","VI2","Visual Inspection 2", 1,true)
        );

        var mockProductConsequenceTemperature = Mockito.mock(ProductConsequence.class);

        var mockProductConsequenceTransit = Mockito.mock(ProductConsequence.class);

        List<ProductConsequence> temperatureConsequences = Collections.singletonList(mockProductConsequenceTemperature);

        List<ProductConsequence> transitTimeConsequences = Collections.singletonList(mockProductConsequenceTransit);


        when(productConsequenceRepository.findAllByProductCategory(anyString())).thenReturn(Flux.just(Mockito.mock(ProductConsequence.class)));

        // Mock repository responses
        when(lookupRepository.findAllByType("TRANSIT_TIME_ZONE"))
            .thenReturn(Flux.fromIterable(transitTimeZones));
        when(lookupRepository.findAllByType("VISUAL_INSPECTION_STATUS"))
            .thenReturn(Flux.fromIterable(visualInspections));
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(productCategory, "TEMPERATURE"))
            .thenReturn(Flux.fromIterable(temperatureConsequences));
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(productCategory, "TRANSIT_TIME"))
            .thenReturn(Flux.fromIterable(transitTimeConsequences));

        var location = Mockito.mock(Location.class);
        when(location.getTimeZone()).thenReturn("America/New_York");

        when(locationRepository.findOneByCode("LOC456")).thenReturn(Mono.just(location));

        // When
        ShippingInformation result = ShippingInformation.fromNewImportBatch(
            command,
            lookupRepository,
            productConsequenceRepository,
            locationRepository
        );

        // Then
        assertNotNull(result);
        assertEquals(productCategory, result.getProductCategory());
        assertEquals("celsius", result.getTemperatureUnit());
        assertTrue(result.isDisplayTransitInformation());
        assertTrue(result.isDisplayTemperature());
        assertEquals(transitTimeZones, result.getTransitTimeZoneList());
        assertEquals(visualInspections, result.getVisualInspectionList());
        assertEquals("America/New_York",result.getDefaultTimeZone());
    }

    @Test
    void shouldCreateShippingInformationWithoutTemperatureAndTransitTime() {
        // Given
        String productCategory = "BASIC";
        EnterShippingInformationCommand command = new EnterShippingInformationCommand(
            productCategory,
            "EMP123",
            "LOC456"
        );



        List<Lookup> visualInspections = Arrays.asList(
            Lookup.fromRepository(1L,"TIME_ZONE","VI1","Visual Inspection 1", 1,true),
            Lookup.fromRepository(1L,"TIME_ZONE","VI2","Visual Inspection 2", 1,true)
        );

        // Mock repository responses
        when(lookupRepository.findAllByType("VISUAL_INSPECTION_STATUS")).thenReturn(Flux.fromIterable(visualInspections));
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(anyString(), anyString())).thenReturn(Flux.empty());
        when(productConsequenceRepository.findAllByProductCategory(anyString())).thenReturn(Flux.just(Mockito.mock(ProductConsequence.class)));

        // When
        ShippingInformation result = ShippingInformation.fromNewImportBatch(
            command,
            lookupRepository,
            productConsequenceRepository,
            locationRepository
        );

        // Then
        assertNotNull(result);
        assertEquals(productCategory, result.getProductCategory());
        assertEquals("celsius", result.getTemperatureUnit());
        assertFalse(result.isDisplayTransitInformation());
        assertFalse(result.isDisplayTemperature());
        assertTrue(result.getTransitTimeZoneList().isEmpty());
        assertEquals(visualInspections, result.getVisualInspectionList());
        assertNull(result.getDefaultTimeZone());
    }

    @Test
    void shouldThrowExceptionWhenInvalidProductCategory() {
        // Given
        when(productConsequenceRepository.findAllByProductCategory(anyString())).thenReturn(Flux.empty());

        EnterShippingInformationCommand command = new EnterShippingInformationCommand(
            "ELECTRONICS",
            "EMP123",
            "LOC456"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ShippingInformation.fromNewImportBatch(command, lookupRepository, productConsequenceRepository,locationRepository));
        assertEquals("Product category is not configured", exception.getMessage());


    }

    @Test
    void shouldThrowExceptionWhenLookupRepositoryIsNull() {
        // Given
        when(productConsequenceRepository.findAllByProductCategory(anyString())).thenReturn(Flux.just(Mockito.mock(ProductConsequence.class)));
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(anyString(), anyString())).thenReturn(Flux.empty());
        EnterShippingInformationCommand command = new EnterShippingInformationCommand(
            "ELECTRONICS",
            "EMP123",
            "LOC456"
        );

        // When & Then

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ShippingInformation.fromNewImportBatch(command, null, productConsequenceRepository,locationRepository));
        assertEquals("LookupRepository is required", exception.getMessage());

    }

    @Test
    void shouldThrowExceptionWhenProductConsequenceRepositoryIsNull() {
        // Given
        EnterShippingInformationCommand command = new EnterShippingInformationCommand(
            "ELECTRONICS",
            "EMP123",
            "LOC456"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ShippingInformation.fromNewImportBatch(command, lookupRepository, null,locationRepository));
        assertEquals("ProductConsequenceRepository is required", exception.getMessage());

    }

    @Test
    void shouldThrowExceptionWhenProductCategoryIsNull() {
        // Given

        EnterShippingInformationCommand command = Mockito.mock(EnterShippingInformationCommand.class);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ShippingInformation.fromNewImportBatch(command, lookupRepository, productConsequenceRepository,locationRepository));
        assertEquals("Product category is required", exception.getMessage());

    }

    @Test
    void shouldCreateShippingInformationSuccessfullyWhenTransferReceipt() {
        // Given
        String productCategory = "ROOM_TEMPERATURE";
        EnterShippingInformationCommand command = new EnterShippingInformationCommand(
            productCategory,
            "EMP123",
            "LOC456"
        );



        List<Lookup> transitTimeZones = Arrays.asList(
            Lookup.fromRepository(1L,"TIME_ZONE","TZ1","Transit Zone 1", 1,true),
            Lookup.fromRepository(2L,"TIME_ZONE","TZ2","Transit Zone 2", 1,true)

        );

        List<Lookup> visualInspections = Arrays.asList(
            Lookup.fromRepository(1L,"TIME_ZONE","VI1","Visual Inspection 1", 1,true),
            Lookup.fromRepository(1L,"TIME_ZONE","VI2","Visual Inspection 2", 1,true)
        );

        var mockProductConsequenceTemperature = Mockito.mock(ProductConsequence.class);

        var mockProductConsequenceTransit = Mockito.mock(ProductConsequence.class);

        List<ProductConsequence> temperatureConsequences = Collections.singletonList(mockProductConsequenceTemperature);

        List<ProductConsequence> transitTimeConsequences = Collections.singletonList(mockProductConsequenceTransit);


        when(productConsequenceRepository.findAllByProductCategory(anyString())).thenReturn(Flux.just(Mockito.mock(ProductConsequence.class)));

        // Mock repository responses
        when(lookupRepository.findAllByType("TRANSIT_TIME_ZONE"))
            .thenReturn(Flux.fromIterable(transitTimeZones));
        when(lookupRepository.findAllByType("VISUAL_INSPECTION_STATUS"))
            .thenReturn(Flux.fromIterable(visualInspections));
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(productCategory, "TEMPERATURE"))
            .thenReturn(Flux.fromIterable(temperatureConsequences));
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(productCategory, "TRANSIT_TIME"))
            .thenReturn(Flux.fromIterable(transitTimeConsequences));

        var location = Mockito.mock(Location.class);
        when(location.getTimeZone()).thenReturn("America/New_York");

        when(locationRepository.findOneByCode("LOC456")).thenReturn(Mono.just(location));
        when(locationRepository.findOneByCode("LOC458")).thenReturn(Mono.just(location));

        InternalTransfer internalTransfer = Mockito.mock(InternalTransfer.class);
        when(internalTransfer.getLocationCodeFrom()).thenReturn("LOC458");
        when(internalTransfer.getOrderNumber()).thenReturn(1L);




        // When
        ShippingInformation result = ShippingInformation.fromNewTransferReceipt(
            command,
            lookupRepository,
            productConsequenceRepository,
            locationRepository,internalTransfer
        );

        // Then
        assertNotNull(result);
        assertEquals(productCategory, result.getProductCategory());
        assertEquals("celsius", result.getTemperatureUnit());
        assertTrue(result.isDisplayTransitInformation());
        assertTrue(result.isDisplayTemperature());
        assertEquals(transitTimeZones, result.getTransitTimeZoneList());
        assertEquals(visualInspections, result.getVisualInspectionList());
        assertEquals("America/New_York",result.getDefaultTimeZone());
        assertEquals(1L,result.getOrderNumber());
        assertTrue(result.isReceivedDifferentLocation());
        assertEquals("America/New_York",result.getDefaultStartTimeZone());
    }

}

