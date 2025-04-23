package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryVolumeDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.InventoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InventoryMapperTest {

    private InventoryMapper inventoryMapper;

    @BeforeEach
    void setUp() {
        // Mapstruct generates an implementation with "Impl" suffix
        inventoryMapper = Mappers.getMapper(InventoryMapper.class);
    }

    @Test
    void toModel_WhenValidInput_ShouldMapAllFields() {
        // Arrange
        var dto = InventoryValidationResponseDTO.builder()
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .id(UUID.randomUUID())
                .unitNumber("W036898786799")
                .locationCode("123456789")
                .productCode("E0701V00")
                .productDescription("Description")
                .expirationDate(LocalDateTime.now())
                .aboRh("AP")
                .productFamily("Product family")
                .collectionDate(ZonedDateTime.now())
                .storageLocation("Storage Location")
                .createDate(ZonedDateTime.now())
                .modificationDate(ZonedDateTime.now())
                .volumes(List.of(InventoryVolumeDTO.builder()
                    .type("volume")
                    .value(150)
                    .unit("MILLILITERS")
                    .build()))
                .build())
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                .builder()
                    .errorName("NAME")
                    .errorCode(1)
                    .errorMessage("Notification message")
                    .errorType("TYPE")
                    .action("ACTION")
                    .reason("REASON")
                    .details(List.of("DETAILS_1"))
                .build()))
            .build();

        // Add other relevant fields based on your DTO structure

        // Act
        InventoryValidation result = inventoryMapper.toValidationModel(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.inventoryResponseDTO().id(), result.getInventory().getId());
        assertEquals(dto.inventoryResponseDTO().unitNumber(), result.getInventory().getUnitNumber());
        assertEquals(dto.inventoryResponseDTO().productCode(), result.getInventory().getProductCode());
        assertEquals(dto.inventoryResponseDTO().locationCode(), result.getInventory().getLocationCode());
        assertEquals(dto.inventoryResponseDTO().productDescription(), result.getInventory().getProductDescription());
        assertEquals(dto.inventoryResponseDTO().expirationDate(), result.getInventory().getExpirationDate());
        assertEquals(dto.inventoryResponseDTO().aboRh(), result.getInventory().getAboRh());
        assertEquals(dto.inventoryResponseDTO().productFamily(), result.getInventory().getProductFamily());
        assertEquals(dto.inventoryResponseDTO().collectionDate(), result.getInventory().getCollectionDate());
        assertEquals(dto.inventoryResponseDTO().storageLocation(), result.getInventory().getStorageLocation());
        assertEquals(dto.inventoryResponseDTO().createDate(), result.getInventory().getCreateDate());
        assertEquals(dto.inventoryResponseDTO().modificationDate(), result.getInventory().getModificationDate());
        assertEquals(dto.inventoryResponseDTO().volumes().size(), result.getInventory().getVolumes().size());
        assertEquals(dto.inventoryResponseDTO().volumes().get(0).type(), result.getInventory().getVolumes().get(0).getType());
        assertEquals(dto.inventoryResponseDTO().volumes().get(0).value(), result.getInventory().getVolumes().get(0).getValue());
        assertEquals(dto.inventoryResponseDTO().volumes().get(0).unit(), result.getInventory().getVolumes().get(0).getUnit());
        assertEquals(dto.inventoryNotificationsDTO().size(), result.getNotifications().size());
        assertEquals(dto.inventoryNotificationsDTO().get(0).errorName(), result.getNotifications().get(0).getErrorName());
        assertEquals(dto.inventoryNotificationsDTO().get(0).errorCode(), result.getNotifications().get(0).getErrorCode());
        assertEquals(dto.inventoryNotificationsDTO().get(0).errorMessage(), result.getNotifications().get(0).getErrorMessage());
        assertEquals(dto.inventoryNotificationsDTO().get(0).errorType(), result.getNotifications().get(0).getErrorType());
        assertEquals(dto.inventoryNotificationsDTO().get(0).action(), result.getNotifications().get(0).getAction());
        assertEquals(dto.inventoryNotificationsDTO().get(0).reason(), result.getNotifications().get(0).getReason());
        assertEquals(dto.inventoryNotificationsDTO().get(0).details().size(), result.getNotifications().get(0).getDetails().size());
        assertEquals(dto.inventoryNotificationsDTO().get(0).details().get(0), result.getNotifications().get(0).getDetails().get(0));



    }

    @Test
    void toModel_WhenNullInput_ShouldReturnNull() {
        // Act
        InventoryValidation result = inventoryMapper.toValidationModel(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toModel_WhenEmptyDTO_ShouldMapWithDefaultValues() {
        // Arrange
        InventoryValidationResponseDTO dto = InventoryValidationResponseDTO.builder().build();

        // Act
        InventoryValidation result = inventoryMapper.toValidationModel(dto);

        // Assert
        assertNotNull(result);
        // Assert default values based on your mapping logic
        assertNull(result.getInventory());
        assertNull(result.getNotifications());
        // Add other assertions for default values
    }


}
