package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateTemperatureCommand;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Test
    void validateTemperature_WhenCommandIsNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Import.validateTemperature(null, productConsequenceRepository)
        );
        assertEquals("Temperature Information is required", exception.getMessage());
    }

    @Test
    void validateTemperature_WhenRepositoryIsNull_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-18.0"),"FROZEN");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Import.validateTemperature(command, null)
        );
        assertEquals("ProductConsequenceRepository is required", exception.getMessage());
    }

    @Test
    void validateTemperature_WhenNoConsequenceFound_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-18.0"),"FROZEN");

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(
            anyString(), anyString()
        )).thenReturn(Flux.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Import.validateTemperature(command, productConsequenceRepository)
        );
        assertEquals("Product Consequence not found.", exception.getMessage());
    }

    @Test
    void validateTemperature_WhenTemperatureIsAcceptable_ReturnsValidResult() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("8.0"),"FROZEN");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TEMPERATURE >= 1 && TEMPERATURE <= 10");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);


        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty("FROZEN", "TEMPERATURE")).thenReturn(Flux.just(consequence));

        ValidationResult result = Import.validateTemperature(command, productConsequenceRepository);

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

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(            "FROZEN", "TEMPERATURE")).thenReturn(Flux.just(consequence,consequence2));

        ValidationResult result = Import.validateTemperature(command, productConsequenceRepository);

        assertFalse(result.valid());
        assertEquals("Temperature does not meet thresholds all products will be quarantined",
            result.message());
    }

    @Test
    void validateTemperature_WhenExpressionIsInvalid_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-15.0"),"FROZEN");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("invalid expression");

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(
            "FROZEN", "TEMPERATURE"
        )).thenReturn(Flux.just(consequence));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Import.validateTemperature(command, productConsequenceRepository)
        );
        assertEquals("Product Consequence not found.", exception.getMessage());
    }

    @Test
    void validateTemperature_WhenResultValueIsNull_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-15.0"),"FROZEN");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn(null);

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(
            "FROZEN", "TEMPERATURE"
        )).thenReturn(Flux.just(consequence));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Import.validateTemperature(command, productConsequenceRepository)
        );
        assertEquals("Product Consequence not found.", exception.getMessage());
    }
}

