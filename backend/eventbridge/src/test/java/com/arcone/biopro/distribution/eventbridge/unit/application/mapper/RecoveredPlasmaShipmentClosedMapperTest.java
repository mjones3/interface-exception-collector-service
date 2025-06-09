package com.arcone.biopro.distribution.eventbridge.unit.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentCartonClosedDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentCartonItemClosedDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentClosedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.RecoveredPlasmaShipmentClosedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedCartonItemOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecoveredPlasmaShipmentClosedMapperTest {

    private RecoveredPlasmaShipmentClosedMapper mapper;
    private RecoveredPlasmaShipmentClosedPayload mockPayload;
    private RecoveredPlasmaShipmentCartonClosedDTO mockCartonClosed;
    private RecoveredPlasmaShipmentCartonItemClosedDTO mockCartonItemDTO;

    @BeforeEach
    void setUp() {
        mapper = new RecoveredPlasmaShipmentClosedMapper();
        mockPayload = mock(RecoveredPlasmaShipmentClosedPayload.class);
        mockCartonClosed = mock(RecoveredPlasmaShipmentCartonClosedDTO.class);
        mockCartonItemDTO = mock(RecoveredPlasmaShipmentCartonItemClosedDTO.class);
    }

    @Test
    @DisplayName("Should map payload to domain object without cartons")
    void shouldMapPayloadToDomainWithoutCartons() {
        // Given
        String shipmentNumber = "SHP123";
        String locationShipmentCode = "LSC456";
        String locationCartonCode = "LCC789";
        String customerCode = "CUST001";
        LocalDate shipmentDate = LocalDate.now();
        ZonedDateTime closeDate = ZonedDateTime.now();
        String locationCode = "LOC001";

        when(mockPayload.shipmentNumber()).thenReturn(shipmentNumber);
        when(mockPayload.locationShipmentCode()).thenReturn(locationShipmentCode);
        when(mockPayload.locationCartonCode()).thenReturn(locationCartonCode);
        when(mockPayload.customerCode()).thenReturn(customerCode);
        when(mockPayload.shipmentDate()).thenReturn(shipmentDate);
        when(mockPayload.closeDate()).thenReturn(closeDate);
        when(mockPayload.locationCode()).thenReturn(locationCode);
        when(mockPayload.cartonList()).thenReturn(null);

        // When
        RecoveredPlasmaShipmentClosedOutbound result = mapper.toDomain(mockPayload);

        // Then
        assertNotNull(result);
        assertEquals(shipmentNumber, result.getShipmentNumber());
        assertEquals(locationShipmentCode, result.getLocationShipmentCode());
        assertEquals(locationCartonCode, result.getLocationCartonCode());
        assertEquals(customerCode, result.getCustomerCode());
        assertEquals(shipmentDate, result.getShipmentDate());
        assertEquals(closeDate, result.getCloseDate());
        assertEquals(locationCode, result.getShipmentLocationCode());
        assertEquals(0, result.getTotalShipmentProducts());
    }

    @Test
    @DisplayName("Should map payload to domain object with cartons")
    void shouldMapPayloadToDomainWithCartons() {
        // Given
        setupBasicPayloadMocks();
        List<RecoveredPlasmaShipmentCartonClosedDTO> cartonList = new ArrayList<>();
        cartonList.add(mockCartonClosed);

        List<RecoveredPlasmaShipmentCartonItemClosedDTO> packedProducts = new ArrayList<>();
        packedProducts.add(mockCartonItemDTO);

        when(mockPayload.cartonList()).thenReturn(cartonList);
        when(mockCartonClosed.cartonNumber()).thenReturn("CARTON123");
        when(mockCartonClosed.totalProducts()).thenReturn(5);
        when(mockCartonClosed.packedProducts()).thenReturn(packedProducts);

        setupCartonItemDTOMocks();

        // When
        RecoveredPlasmaShipmentClosedOutbound result = mapper.toDomain(mockPayload);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getTotalShipmentProducts());
    }

    @Test
    @DisplayName("Should map carton item DTO to domain object")
    void shouldMapCartonItemDTOToDomain() {
        // Given
        setupCartonItemDTOMocks();

        // When
        RecoveredPlasmaShipmentClosedCartonItemOutbound result = mapper.toDomain(mockCartonItemDTO);

        // Then
        assertNotNull(result);
        assertEquals("UNIT123", result.getUnitNumber());
        assertEquals("PROD456", result.getProductCode());
        assertEquals("FAC789", result.getCollectionFacility());
        assertEquals(new BigDecimal("0.255"), result.getProductVolume());
        assertEquals("A+", result.getBloodType());
        assertEquals("America/New_York", result.getCollectionTimeZone());
    }

    @Test
    @DisplayName("Should handle empty carton list")
    void shouldHandleEmptyCartonList() {
        // Given
        setupBasicPayloadMocks();
        when(mockPayload.cartonList()).thenReturn(new ArrayList<>());

        // When
        RecoveredPlasmaShipmentClosedOutbound result = mapper.toDomain(mockPayload);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalShipmentProducts());
    }

    private void setupBasicPayloadMocks() {
        when(mockPayload.shipmentNumber()).thenReturn("SHP123");
        when(mockPayload.locationShipmentCode()).thenReturn("LSC456");
        when(mockPayload.locationCartonCode()).thenReturn("LCC789");
        when(mockPayload.customerCode()).thenReturn("CUST001");
        when(mockPayload.shipmentDate()).thenReturn(LocalDate.now());
        when(mockPayload.closeDate()).thenReturn(ZonedDateTime.now());
        when(mockPayload.locationCode()).thenReturn("LOC001");
    }

    private void setupCartonItemDTOMocks() {
        when(mockCartonItemDTO.unitNumber()).thenReturn("UNIT123");
        when(mockCartonItemDTO.productCode()).thenReturn("PROD456");
        when(mockCartonItemDTO.collectionFacility()).thenReturn("FAC789");
        when(mockCartonItemDTO.donationDate()).thenReturn(ZonedDateTime.now());
        when(mockCartonItemDTO.volume()).thenReturn(255);
        when(mockCartonItemDTO.aboRh()).thenReturn("A+");
        when(mockCartonItemDTO.collectionTimeZone()).thenReturn("America/New_York");
    }
}
