package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.BarcodePattern;
import com.arcone.biopro.distribution.receiving.domain.model.BarcodeTranslation;
import com.arcone.biopro.distribution.receiving.domain.model.BarcodeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.FinNumber;
import com.arcone.biopro.distribution.receiving.domain.model.Product;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateBarcodeCommand;
import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarcodeValidatorTest {

    @Mock
    private ConfigurationService configurationService;

    private BarcodePattern pattern;
    private ValidateBarcodeCommand command;

    @BeforeEach
    void setUp() {
        pattern = Mockito.mock(BarcodePattern.class);
        command = Mockito.mock(ValidateBarcodeCommand.class);
    }

    @Test
    void validateBarcode_NullCommand_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> BarcodeValidator.validateBarcode(null, configurationService));
    }

    @Test
    void validateBarcode_NullConfigService_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> BarcodeValidator.validateBarcode(command, null));
    }

    @Test
    void validateBarcode_EmptyBarcodePatternConfigService_ThrowsException() {

        when(configurationService.findByParseType(any())).thenReturn(Mono.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> BarcodeValidator.validateBarcode(command, configurationService)) ;
        assertEquals("Barcode Pattern is required", exception.getMessage());
    }

    @Test
    void validateBarcode_InvalidBarcodePattern_ReturnsInvalid() {

        when(pattern.getPattern()).thenReturn("[A-Z]+");
        when(pattern.getMatchGroups()).thenReturn(0);
        when(command.getBarcodeValue()).thenReturn("123");

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertFalse(result.valid());
        assertEquals("Barcode is not valid", result.message());
    }

    @Test
    void validateBarcode_ValidUnitNumber_ReturnsValid() {

        when(pattern.getPattern()).thenReturn("(\\=)([A-Z]{1}\\w{12})(00)");
        when(pattern.getMatchGroups()).thenReturn(2);

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        when(command.getParseType()).thenReturn(ParseType.BARCODE_UNIT_NUMBER);
        when(command.getBarcodeValue()).thenReturn("=W03689878680000");

        FinNumber translation = Mockito.mock(FinNumber.class);
        when(configurationService.findByFinNumber("W0368")).thenReturn(Mono.just(translation));

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertTrue(result.valid());
        assertEquals("W036898786800", result.result());
    }

    @Test
    void validateBarcode_InvalidUnitNumber_ReturnsInvalid() {

        when(pattern.getPattern()).thenReturn("(\\=)([A-Z]{1}\\w{12})(00)");
        when(pattern.getMatchGroups()).thenReturn(2);

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        when(command.getParseType()).thenReturn(ParseType.BARCODE_UNIT_NUMBER);
        when(command.getBarcodeValue()).thenReturn("=W03689878680000");

        when(configurationService.findByFinNumber("W0368")).thenReturn(Mono.empty());

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertFalse(result.valid());
        assertEquals("FIN is not associated with a registered facility", result.message());
    }

    @Test
    void validateBarcode_ValidProductCode_ReturnsValid() {
        when(pattern.getPattern()).thenReturn("(\\w+)");
        when(pattern.getMatchGroups()).thenReturn(1);

        when(command.getParseType()).thenReturn(ParseType.BARCODE_PRODUCT_CODE);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getBarcodeValue()).thenReturn("PROD123");

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        Product translation = Mockito.mock(Product.class);
        when(configurationService.findByCodeAndTemperatureCategory("PROD123", "FROZEN"))
            .thenReturn(Mono.just(translation));

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertTrue(result.valid());
        assertEquals("PROD123", result.result());
    }

    @Test
    void validateBarcode_ValidProductCode_Product_Family_Not_Found_ReturnsInValid() {
        when(pattern.getPattern()).thenReturn("(\\w+)");
        when(pattern.getMatchGroups()).thenReturn(1);

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        when(command.getParseType()).thenReturn(ParseType.BARCODE_PRODUCT_CODE);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getBarcodeValue()).thenReturn("PROD123");

        when(configurationService.findByCodeAndTemperatureCategory("PROD123", "FROZEN")).thenReturn(Mono.empty());

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertFalse(result.valid());
        assertEquals("Product type does not match", result.message());
    }

    @Test
    void validateBarcode_ValidExpirationDate_ReturnsValid() {

        when(pattern.getPattern()).thenReturn("(\\&\\>)(\\d{3}\\d{3}\\d{2}\\d{2})");
        when(pattern.getMatchGroups()).thenReturn(2);

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        when(command.getParseType()).thenReturn(ParseType.BARCODE_EXPIRATION_DATE);
        when(command.getBarcodeValue()).thenReturn("&>0260422359");

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertTrue(result.valid());
        assertNotNull(result.result());
        assertEquals("Feb 11, 2026",result.resultDescription());
        assertEquals("2026-11-02T23:59:59Z",result.result());
    }

    @Test
    void validateBarcode_InvalidExpirationDate_ReturnsInvalid() {

        when(pattern.getPattern()).thenReturn("(\\&\\>)(\\d{3}\\d{3}\\d{2}\\d{2})");
        when(pattern.getMatchGroups()).thenReturn(2);


        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        when(command.getParseType()).thenReturn(ParseType.BARCODE_EXPIRATION_DATE);
        when(command.getBarcodeValue()).thenReturn("&>0260422360");

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertFalse(result.valid());
        assertEquals("Invalid Expiration Date", result.message());
    }

    @Test
    void validateBarcode_ValidBloodGroup_ReturnsValid() {

        when(pattern.getPattern()).thenReturn("(\\=\\%)(\\w{4})");
        when(pattern.getMatchGroups()).thenReturn(2);

        when(command.getParseType()).thenReturn(ParseType.BARCODE_BLOOD_GROUP);
        when(command.getBarcodeValue()).thenReturn("=%1700");

        when(configurationService.findByParseType(any())).thenReturn(Mono.just(pattern));

        BarcodeTranslation translation = Mockito.mock(BarcodeTranslation.class);
        when(translation.getToValue()).thenReturn("BN");

        when(configurationService.findByFromValueAndSixthDigit("1700", "V")).thenReturn(Mono.just(translation));

        ValidationResult result = BarcodeValidator.validateBarcode(command, configurationService);

        assertTrue(result.valid());
        assertNotNull(result.result());
        assertEquals("B Negative",result.resultDescription());
        assertEquals("BN",result.result());
    }
}
