package com.arcone.biopro.distribution.shipping.unit.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ProductResponseMapper;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductResponseMapperTest {

    private ProductResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ProductResponseMapper.class);
    }

    @Test
    void shouldMapInventoryResponseDTOToProductResponseDTO() {
        // Given
        InventoryResponseDTO inventoryDTO = InventoryResponseDTO.builder()
            .id(UUID.randomUUID())
            .unitNumber("UNIT123")
            .productCode("PROD123")
            .aboRh("A+")
            .productDescription("Test Product")
            .productFamily("PLASMA")
            .status("AVAILABLE")
            .isLabeled(true)
            .isLicensed(false)
            .build();

        // When
        ProductResponseDTO result = mapper.toResponseDTO(inventoryDTO);

        // Then
        assertNotNull(result);
        assertEquals("UNIT123", result.unitNumber());
        assertEquals("PROD123", result.productCode());
        assertEquals("A+", result.aboRh());
        assertEquals("Test Product", result.productDescription());
        assertEquals("PLASMA", result.productFamily());
        assertEquals("AVAILABLE", result.status());
        assertTrue(result.isLabeled());
        assertFalse(result.isLicensed());
    }

    @Test
    void shouldMapInventoryResponseDTOListToProductResponseDTOList() {
        // Given
        List<InventoryResponseDTO> inventoryList = Arrays.asList(
            InventoryResponseDTO.builder()
                .unitNumber("UNIT123")
                .productCode("PROD123")
                .status("AVAILABLE")
                .build(),
            InventoryResponseDTO.builder()
                .unitNumber("UNIT456")
                .productCode("PROD456")
                .status("QUARANTINED")
                .build()
        );

        // When
        List<ProductResponseDTO> result = mapper.toResponseDTO(inventoryList);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("UNIT123", result.get(0).unitNumber());
        assertEquals("UNIT456", result.get(1).unitNumber());
    }

    @Test
    void shouldMapShipmentItemPackedListToProductResponseDTOList() {
        // Given
        List<ShipmentItemPacked> packedList = Arrays.asList(
            ShipmentItemPacked.builder()
                .id(1L)
                .unitNumber("UNIT123")
                .productCode("PROD123")
                .aboRh("A+")
                .productDescription("Test Product 1")
                .productFamily("PLASMA")
                .productStatus("AVAILABLE")
                .build(),
            ShipmentItemPacked.builder()
                .id(2L)
                .unitNumber("UNIT456")
                .productCode("PROD456")
                .aboRh("B-")
                .productDescription("Test Product 2")
                .productFamily("PLATELETS")
                .productStatus("QUARANTINED")
                .build()
        );

        // When
        List<ProductResponseDTO> result = mapper.toProductResponseDTO(packedList);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("UNIT123", result.get(0).unitNumber());
        assertEquals("PROD123", result.get(0).productCode());
        assertEquals("A+", result.get(0).aboRh());
        assertEquals("Test Product 1", result.get(0).productDescription());
        assertEquals("PLASMA", result.get(0).productFamily());
        assertEquals("UNIT456", result.get(1).unitNumber());
    }

    @Test
    void shouldHandleNullInventoryResponseDTO() {
        // When
        ProductResponseDTO result = mapper.toResponseDTO((InventoryResponseDTO) null);

        // Then
        assertNull(result);
    }

    @Test
    void shouldHandleEmptyInventoryResponseDTOList() {
        // When
        List<ProductResponseDTO> result = mapper.toResponseDTO(Collections.emptyList());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleEmptyShipmentItemPackedList() {
        // When
        List<ProductResponseDTO> result = mapper.toProductResponseDTO(Collections.emptyList());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMapInventoryResponseDTOWithNullFields() {
        // Given
        InventoryResponseDTO inventoryDTO = InventoryResponseDTO.builder()
            .unitNumber("UNIT123")
            .productCode(null)
            .aboRh(null)
            .productDescription(null)
            .productFamily(null)
            .status("AVAILABLE")
            .isLabeled(null)
            .isLicensed(null)
            .build();

        // When
        ProductResponseDTO result = mapper.toResponseDTO(inventoryDTO);

        // Then
        assertNotNull(result);
        assertEquals("UNIT123", result.unitNumber());
        assertNull(result.productCode());
        assertNull(result.aboRh());
        assertNull(result.productDescription());
        assertNull(result.productFamily());
        assertEquals("AVAILABLE", result.status());
        assertNull(result.isLabeled());
        assertNull(result.isLicensed());
    }

    @Test
    void shouldMapShipmentItemPackedWithNullFields() {
        // Given
        List<ShipmentItemPacked> packedList = Arrays.asList(
            ShipmentItemPacked.builder()
                .id(1L)
                .unitNumber("UNIT123")
                .productCode(null)
                .aboRh(null)
                .productDescription(null)
                .productFamily(null)
                .productStatus(null)
                .build()
        );

        // When
        List<ProductResponseDTO> result = mapper.toProductResponseDTO(packedList);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("UNIT123", result.get(0).unitNumber());
        assertNull(result.get(0).productCode());
        assertNull(result.get(0).aboRh());
        assertNull(result.get(0).productDescription());
        assertNull(result.get(0).productFamily());
        assertNull(result.get(0).status());
    }
}

