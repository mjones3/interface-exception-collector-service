package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentLineItemProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentLineItemProductTest {

    @Test
    @DisplayName("Should create ShipmentLineItemProduct with valid parameters")
    void shouldCreateShipmentLineItemProductWithValidParameters() {
        // Arrange
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String bloodType = "A+";
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
        ZonedDateTime collectionDate = ZonedDateTime.now();

        // Act
        ShipmentLineItemProduct product = new ShipmentLineItemProduct(
            unitNumber,
            productCode,
            bloodType,
            expirationDate,
            collectionDate
        );

        // Assert
        assertNotNull(product);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when unitNumber is null")
    void shouldThrowExceptionWhenUnitNumberIsNull() {
        // Arrange
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
        ZonedDateTime collectionDate = ZonedDateTime.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ShipmentLineItemProduct(
                null,
                "PROD001",
                "A+",
                expirationDate,
                collectionDate
            );
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when productCode is null")
    void shouldThrowExceptionWhenProductCodeIsNull() {
        // Arrange
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
        ZonedDateTime collectionDate = ZonedDateTime.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ShipmentLineItemProduct(
                "UNIT001",
                null,
                "A+",
                expirationDate,
                collectionDate
            );
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when bloodType is null")
    void shouldThrowExceptionWhenBloodTypeIsNull() {
        // Arrange
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
        ZonedDateTime collectionDate = ZonedDateTime.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ShipmentLineItemProduct(
                "UNIT001",
                "PROD001",
                null,
                expirationDate,
                collectionDate
            );
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when expirationDate is null")
    void shouldThrowExceptionWhenExpirationDateIsNull() {
        // Arrange
        ZonedDateTime collectionDate = ZonedDateTime.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ShipmentLineItemProduct(
                "UNIT001",
                "PROD001",
                "A+",
                null,
                collectionDate
            );
        });
    }

    @Test
    @DisplayName("Should add attribute successfully")
    void shouldAddAttributeSuccessfully() {
        // Arrange
        ShipmentLineItemProduct product = new ShipmentLineItemProduct(
            "UNIT001",
            "PROD001",
            "A+",
            LocalDateTime.now().plusDays(30),
            ZonedDateTime.now()
        );

        // Act
        product.addAttribute("temperature", "4C");

        // Assert
        assertDoesNotThrow(() -> product.addAttribute("key", "value"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attribute key is null")
    void shouldThrowExceptionWhenAttributeKeyIsNull() {
        // Arrange
        ShipmentLineItemProduct product = new ShipmentLineItemProduct(
            "UNIT001",
            "PROD001",
            "A+",
            LocalDateTime.now().plusDays(30),
            ZonedDateTime.now()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            product.addAttribute(null, "value");
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attribute value is null")
    void shouldThrowExceptionWhenAttributeValueIsNull() {
        // Arrange
        ShipmentLineItemProduct product = new ShipmentLineItemProduct(
            "UNIT001",
            "PROD001",
            "A+",
            LocalDateTime.now().plusDays(30),
            ZonedDateTime.now()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            product.addAttribute("key", null);
        });
    }
}

