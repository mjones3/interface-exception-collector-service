package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryCartonItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShippingSummaryCartonItemTest {

    @Test
    @DisplayName("Should create ShippingSummaryCartonItem successfully when all fields are valid")
    void shouldCreateShippingSummaryCartonItemSuccessfully() {
        // Arrange
        CartonItem product = mock(CartonItem.class);
        when(product.getProductCode()).thenReturn("P123");
        when(product.getProductDescription()).thenReturn("Test Product");

        Carton carton = mock(Carton.class);
        when(carton.getCartonNumber()).thenReturn("C123");
        when(carton.getProducts()).thenReturn(List.of(product));
        when(carton.getTotalProducts()).thenReturn(5);

        // Act
        ShippingSummaryCartonItem summaryItem = new ShippingSummaryCartonItem(carton);

        // Assert
        assertEquals("C123", summaryItem.getCartonNumber());
        assertEquals("P123", summaryItem.getProductCode());
        assertEquals("Test Product", summaryItem.getProductDescription());
        assertEquals(5, summaryItem.getTotalProducts());
    }

    @Test
    @DisplayName("Should throw exception when carton number is null")
    void shouldThrowExceptionWhenCartonNumberIsNull() {
        // Arrange
        CartonItem product = mock(CartonItem.class);
        when(product.getProductCode()).thenReturn("P123");
        when(product.getProductDescription()).thenReturn("Test Product");

        Carton carton = mock(Carton.class);
        when(carton.getCartonNumber()).thenReturn(null);
        when(carton.getProducts()).thenReturn(List.of(product));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ShippingSummaryCartonItem(carton)
        );
        assertEquals("Carton Number is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when carton number is blank")
    void shouldThrowExceptionWhenCartonNumberIsBlank() {
        // Arrange
        CartonItem product = mock(CartonItem.class);
        when(product.getProductCode()).thenReturn("P123");
        when(product.getProductDescription()).thenReturn("Test Product");

        Carton carton = mock(Carton.class);
        when(carton.getCartonNumber()).thenReturn("  ");
        when(carton.getProducts()).thenReturn(List.of(product));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ShippingSummaryCartonItem(carton)
        );
        assertEquals("Carton Number is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when product code is null")
    void shouldThrowExceptionWhenProductCodeIsNull() {
        // Arrange
        CartonItem product = mock(CartonItem.class);
        when(product.getProductCode()).thenReturn(null);
        when(product.getProductDescription()).thenReturn("Test Product");

        Carton carton = mock(Carton.class);
        when(carton.getCartonNumber()).thenReturn("C123");
        when(carton.getProducts()).thenReturn(List.of(product));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ShippingSummaryCartonItem(carton)
        );
        assertEquals("Product Code is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when product description is null")
    void shouldThrowExceptionWhenProductDescriptionIsNull() {
        // Arrange
        CartonItem product = mock(CartonItem.class);
        when(product.getProductCode()).thenReturn("P123");
        when(product.getProductDescription()).thenReturn(null);

        Carton carton = mock(Carton.class);
        when(carton.getCartonNumber()).thenReturn("C123");
        when(carton.getProducts()).thenReturn(List.of(product));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ShippingSummaryCartonItem(carton)
        );
        assertEquals("Product Description is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Arrange
        CartonItem product1 = mock(CartonItem.class);
        when(product1.getProductCode()).thenReturn("P123");
        when(product1.getProductDescription()).thenReturn("Test Product");

        Carton carton1 = mock(Carton.class);
        when(carton1.getCartonNumber()).thenReturn("C123");
        when(carton1.getProducts()).thenReturn(List.of(product1));
        when(carton1.getTotalProducts()).thenReturn(5);

        CartonItem product2 = mock(CartonItem.class);
        when(product2.getProductCode()).thenReturn("P123");
        when(product2.getProductDescription()).thenReturn("Test Product");

        Carton carton2 = mock(Carton.class);
        when(carton2.getCartonNumber()).thenReturn("C123");
        when(carton2.getProducts()).thenReturn(List.of(product2));
        when(carton2.getTotalProducts()).thenReturn(5);

        // Act
        ShippingSummaryCartonItem item1 = new ShippingSummaryCartonItem(carton1);
        ShippingSummaryCartonItem item2 = new ShippingSummaryCartonItem(carton2);

        // Assert
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Arrange
        CartonItem product = mock(CartonItem.class);
        when(product.getProductCode()).thenReturn("P123");
        when(product.getProductDescription()).thenReturn("Test Product");

        Carton carton = mock(Carton.class);
        when(carton.getCartonNumber()).thenReturn("C123");
        when(carton.getProducts()).thenReturn(List.of(product));
        when(carton.getTotalProducts()).thenReturn(5);

        // Act
        ShippingSummaryCartonItem summaryItem = new ShippingSummaryCartonItem(carton);
        String toString = summaryItem.toString();

        // Assert
        assertTrue(toString.contains("C123"));
        assertTrue(toString.contains("P123"));
        assertTrue(toString.contains("Test Product"));
        assertTrue(toString.contains("5"));
    }
}


