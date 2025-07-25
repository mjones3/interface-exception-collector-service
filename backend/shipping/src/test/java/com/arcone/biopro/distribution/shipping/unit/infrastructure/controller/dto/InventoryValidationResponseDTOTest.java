package com.arcone.biopro.distribution.shipping.unit.infrastructure.controller.dto;

import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryValidationResponseDTOTest {

    @CsvSource({
        "A,A",
        "A|B|C,A|B|C",
        "A|B|C,C|B|A",
        "B|A|C,C|A|B",
    })
    @ParameterizedTest
    void shouldReturnTrueIfHasOnlyNotificationTypesRegardlessSorting(final String inventoryTypes, final String typesToCompare) {
        var listOfInventoryTypes = Arrays.asList(inventoryTypes.split("\\|"));
        var listOfTypesToCompare = Arrays.asList(typesToCompare.split("\\|"));
        var notifications = listOfInventoryTypes.stream()
            .map(type ->
                InventoryNotificationDTO.builder()
                    .errorName(type)
                    .build()
            )
            .toList();

        var response = new InventoryValidationResponseDTO(null, notifications);
        assertTrue(response.hasOnlyNotificationTypes(listOfTypesToCompare));
    }

    @CsvSource({
        "A|B|C,A|B|C|D",
        "A|B|C,C|B",
        "A,Z"
    })
    @ParameterizedTest
    void shouldReturnFalseIfHasLessOrMoreNotificationTypesRegardlessSorting(final String inventoryTypes, final String typesToCompare) {
        var listOfInventoryTypes = Arrays.asList(inventoryTypes.split("\\|"));
        var listOfTypesToCompare = Arrays.asList(typesToCompare.split("\\|"));
        var notifications = listOfInventoryTypes.stream()
            .map(type ->
                InventoryNotificationDTO.builder()
                    .errorName(type)
                    .build()
            )
            .toList();

        var response = new InventoryValidationResponseDTO(null, notifications);
        assertFalse(response.hasOnlyNotificationTypes(listOfTypesToCompare));
    }

}
