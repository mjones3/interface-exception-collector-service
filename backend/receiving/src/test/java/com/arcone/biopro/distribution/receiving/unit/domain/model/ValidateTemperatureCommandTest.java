package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.ValidateTemperatureCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidateTemperatureCommandTest {

    @Test
    void constructor_WhenValidParameters_CreatesInstance() {
        BigDecimal temperature = new BigDecimal("20.5");
        String category = "FROZEN";

        ValidateTemperatureCommand command = new ValidateTemperatureCommand(temperature, category);

        assertNotNull(command);
        assertEquals(temperature, command.getTemperature());
        assertEquals(category, command.getTemperatureCategory());
    }

    @Test
    void constructor_WhenTemperatureIsNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ValidateTemperatureCommand(null, "FROZEN")
        );

        assertEquals("Temperature is required", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidTemperatureCategories")
    void constructor_WhenTemperatureCategoryIsInvalid_ThrowsException(String invalidCategory) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ValidateTemperatureCommand(new BigDecimal("20.5"), invalidCategory)
        );

        assertEquals("Temperature category is required", exception.getMessage());
    }

    private static Stream<Arguments> invalidTemperatureCategories() {
        return Stream.of(
            Arguments.of((String) null),
            Arguments.of(""),
            Arguments.of("   ")
        );
    }

    @Test
    void checkValid_WhenCalledDirectly_ValidatesCorrectly() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(
            new BigDecimal("20.5"),
            "FROZEN"
        );

        assertDoesNotThrow(command::checkValid);
    }

    @Test
    void getters_ReturnCorrectValues() {
        BigDecimal temperature = new BigDecimal("20.5");
        String category = "FROZEN";

        ValidateTemperatureCommand command = new ValidateTemperatureCommand(temperature, category);

        assertEquals(temperature, command.getTemperature());
        assertEquals(category, command.getTemperatureCategory());
    }

    @Test
    void constructor_WithNegativeTemperature_CreatesInstance() {
        BigDecimal temperature = new BigDecimal("-18.5");
        String category = "FROZEN";

        ValidateTemperatureCommand command = new ValidateTemperatureCommand(temperature, category);

        assertNotNull(command);
        assertEquals(temperature, command.getTemperature());
        assertEquals(category, command.getTemperatureCategory());
    }

    @Test
    void constructor_WithZeroTemperature_CreatesInstance() {
        BigDecimal temperature = BigDecimal.ZERO;
        String category = "CHILLED";

        ValidateTemperatureCommand command = new ValidateTemperatureCommand(temperature, category);

        assertNotNull(command);
        assertEquals(temperature, command.getTemperature());
        assertEquals(category, command.getTemperatureCategory());
    }
}

