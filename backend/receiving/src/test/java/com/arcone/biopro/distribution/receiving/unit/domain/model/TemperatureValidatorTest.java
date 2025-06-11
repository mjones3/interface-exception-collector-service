package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.TemperatureValidator;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateTemperatureCommand;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TemperatureValidatorTest {

    @Test
    void validateTemperature_WhenCommandIsNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TemperatureValidator.validateTemperature(null, Collections.emptyList())
        );
        assertEquals("Temperature Information is required", exception.getMessage());
    }

    @Test
    void validateTemperature_WhenConsequenceListIsNull_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-18.0"),"FROZEN");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TemperatureValidator.validateTemperature(command, null)
        );
        assertEquals("ProductConsequenceList is required", exception.getMessage());
    }


    @Test
    void validateTemperature_WhenTemperatureIsAcceptable_ReturnsValidResult() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("8.0"),"FROZEN");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TEMPERATURE >= 1 && TEMPERATURE <= 10");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        ValidationResult result = TemperatureValidator.validateTemperature(command, List.of(consequence));

        assertTrue(result.valid());
        assertNull(result.message());
    }

    @Test
    void validateTemperature_WhenTemperatureIsNotAcceptable_ReturnsInvalidResult() {

        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-15.0"),"FROZEN");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TEMPERATURE >= 1 && TEMPERATURE <= 10");

        ProductConsequence consequence2 = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence2.getResultValue()).thenReturn("TEMPERATURE > 10 || TEMPERATURE < 1");
        Mockito.when(consequence2.isAcceptable()).thenReturn(false);

        ValidationResult result = TemperatureValidator.validateTemperature(command, List.of(consequence,consequence2));

        assertFalse(result.valid());
        assertEquals("Temperature does not meet thresholds all products will be quarantined",
            result.message());
    }

    @Test
    void validateTemperature_WhenExpressionIsInvalid_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-15.0"),"FROZEN");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("invalid expression");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TemperatureValidator.validateTemperature(command, List.of(consequence))
        );
        assertEquals("Product Consequence not found.", exception.getMessage());
    }

    @Test
    void validateTemperature_WhenResultValueIsNull_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-15.0"),"FROZEN");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TemperatureValidator.validateTemperature(command, List.of(consequence))
        );
        assertEquals("Product Consequence not found.", exception.getMessage());
    }
}

