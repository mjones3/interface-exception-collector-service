package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.Product;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void shouldCreateValidProduct() {
        // Given
        String id = "123";
        String productCode = "PRD001";
        String shortDescription = "Test Product";
        String productFamily = "Test Family";
        boolean active = true;
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // When
        Product product = new Product(id, productCode, shortDescription,
            productFamily, active, createDate, modificationDate);

        // Then
        assertNotNull(product);
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // Given
        String productCode = "PRD001";
        String shortDescription = "Test Product";
        String productFamily = "Test Family";
        boolean active = true;
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product(null, productCode, shortDescription,
                productFamily, active, createDate, modificationDate)
        );
        assertEquals("Id is required.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProductCodeIsBlank() {
        // Given
        String id = "123";
        String shortDescription = "Test Product";
        String productFamily = "Test Family";
        boolean active = true;
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product(id, " ", shortDescription,
                productFamily, active, createDate, modificationDate)
        );
        assertEquals("Product Code is required.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenShortDescriptionIsNull() {
        // Given
        String id = "123";
        String productCode = "PRD001";
        String productFamily = "Test Family";
        boolean active = true;
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product(id, productCode, null,
                productFamily, active, createDate, modificationDate)
        );
        assertEquals("Short Description is required.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProductFamilyIsBlank() {
        // Given
        String id = "123";
        String productCode = "PRD001";
        String shortDescription = "Test Product";
        boolean active = true;
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product(id, productCode, shortDescription,
                "", active, createDate, modificationDate)
        );
        assertEquals("Product Family is required.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateDateIsNull() {
        // Given
        String id = "123";
        String productCode = "PRD001";
        String shortDescription = "Test Product";
        String productFamily = "Test Family";
        boolean active = true;
        ZonedDateTime modificationDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Product(id, productCode, shortDescription,
                productFamily, active, null, modificationDate)
        );
        assertEquals("Create Date is required.", exception.getMessage());
    }
}

