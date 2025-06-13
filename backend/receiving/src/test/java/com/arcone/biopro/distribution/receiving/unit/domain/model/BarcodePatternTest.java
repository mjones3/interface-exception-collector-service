package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.BarcodePattern;
import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BarcodePatternTest {

    @Test
    void shouldCreateBarcodePattern() {
        // Mock the ParseType enum since it's from an external package
        ParseType mockParseType = Mockito.mock(ParseType.class);

        BarcodePattern barcodePattern = new BarcodePattern("(\\=)([A-Z]{1}\\w{12})(00)", 2, mockParseType);

        Assertions.assertNotNull(barcodePattern);
        Assertions.assertEquals("(\\=)([A-Z]{1}\\w{12})(00)", barcodePattern.getPattern());
        Assertions.assertEquals(2, barcodePattern.getMatchGroups());
        Assertions.assertEquals(mockParseType, barcodePattern.getParseType());
    }

    @Test
    void shouldUpdateBarcodePatternProperties() {
        // Mock the ParseType enum since it's from an external package
        ParseType mockParseType = Mockito.mock(ParseType.class);
        ParseType newMockParseType = Mockito.mock(ParseType.class);

        BarcodePattern barcodePattern = new BarcodePattern("(\\=)([A-Z]{1}\\w{12})(00)", 2, mockParseType);

        // Test setters
        barcodePattern.setPattern("(\\=\\%)(\\w{4})");
        barcodePattern.setMatchGroups(3);
        barcodePattern.setParseType(newMockParseType);

        Assertions.assertEquals("(\\=\\%)(\\w{4})", barcodePattern.getPattern());
        Assertions.assertEquals(3, barcodePattern.getMatchGroups());
        Assertions.assertEquals(newMockParseType, barcodePattern.getParseType());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Mock the ParseType enum since it's from an external package
        ParseType mockParseType = Mockito.mock(ParseType.class);

        BarcodePattern pattern1 = new BarcodePattern("(\\=)([A-Z]{1}\\w{12})(00)", 2, mockParseType);
        BarcodePattern pattern2 = new BarcodePattern("(\\=)([A-Z]{1}\\w{12})(00)", 2, mockParseType);
        BarcodePattern pattern3 = new BarcodePattern("(\\=\\%)(\\w{4})", 2, mockParseType);

        Assertions.assertEquals(pattern1, pattern2);
        Assertions.assertNotEquals(pattern1, pattern3);
        Assertions.assertEquals(pattern1.hashCode(), pattern2.hashCode());
    }
}
