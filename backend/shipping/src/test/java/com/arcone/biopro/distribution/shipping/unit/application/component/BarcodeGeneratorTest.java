package com.arcone.biopro.distribution.shipping.unit.application.component;

import com.arcone.biopro.distribution.shipping.application.component.BarcodeGenerator;
import graphql.Assert;
import org.junit.jupiter.api.Test;

import java.util.Base64;

class BarcodeGeneratorTest {

    @Test
    public void shouldGenerateBase64Barcode(){

        var target = new BarcodeGenerator();

        var barcode = target.generateCode128BarcodeBase64("456");

        try {
            Base64.getDecoder().decode(barcode);
            Assert.assertTrue(true);
        } catch (IllegalArgumentException e) {
            org.junit.Assert.fail("Unexpected exception: " + e.getMessage());
        }

    }

}
