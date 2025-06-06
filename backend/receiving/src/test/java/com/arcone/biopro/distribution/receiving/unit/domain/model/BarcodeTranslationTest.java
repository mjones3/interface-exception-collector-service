package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.BarcodeTranslation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BarcodeTranslationTest {

    @Test
    void shouldCreateBarcodeTranslation() {
        BarcodeTranslation translation = new BarcodeTranslation(1L, "9500", "ON", "V");

        Assertions.assertNotNull(translation);
        Assertions.assertEquals(1L, translation.getId());
        Assertions.assertEquals("9500", translation.getFromValue());
        Assertions.assertEquals("ON", translation.getToValue());
        Assertions.assertEquals("V", translation.getSixthDigit());
    }

    @Test
    void shouldCreateBarcodeTranslationWithBuilder() {
        BarcodeTranslation translation = BarcodeTranslation.builder()
                .id(1L)
                .fromValue("9500")
                .toValue("ON")
                .sixthDigit("V")
                .build();

        Assertions.assertNotNull(translation);
        Assertions.assertEquals(1L, translation.getId());
        Assertions.assertEquals("9500", translation.getFromValue());
        Assertions.assertEquals("ON", translation.getToValue());
        Assertions.assertEquals("V", translation.getSixthDigit());
    }

    @Test
    void shouldCreateBarcodeTranslationWithoutSixthDigit() {
        BarcodeTranslation translation = BarcodeTranslation.builder()
                .id(9L)
                .fromValue("5500")
                .toValue("O")
                .build();

        Assertions.assertNotNull(translation);
        Assertions.assertEquals(9L, translation.getId());
        Assertions.assertEquals("5500", translation.getFromValue());
        Assertions.assertEquals("O", translation.getToValue());
        Assertions.assertNull(translation.getSixthDigit());
    }

    @Test
    void shouldUpdateBarcodeTranslationProperties() {
        BarcodeTranslation translation = new BarcodeTranslation(1L, "9500", "ON", "V");
        
        // Test setters
        translation.setId(2L);
        translation.setFromValue("5100");
        translation.setToValue("OP");
        translation.setSixthDigit("X");
        
        Assertions.assertEquals(2L, translation.getId());
        Assertions.assertEquals("5100", translation.getFromValue());
        Assertions.assertEquals("OP", translation.getToValue());
        Assertions.assertEquals("X", translation.getSixthDigit());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        BarcodeTranslation translation1 = new BarcodeTranslation(1L, "9500", "ON", "V");
        BarcodeTranslation translation2 = new BarcodeTranslation(1L, "9500", "ON", "V");
        BarcodeTranslation translation3 = new BarcodeTranslation(2L, "5100", "OP", "V");
        
        Assertions.assertEquals(translation1, translation2);
        Assertions.assertNotEquals(translation1, translation3);
        Assertions.assertEquals(translation1.hashCode(), translation2.hashCode());
    }
}