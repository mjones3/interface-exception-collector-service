package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.ShortDateProduct;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShortDateProductTest {

    @Test
    void testValidatable() {
        assertThrows(IllegalArgumentException.class, () -> new ShortDateProduct(null, null, null, null), "unitNumber cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new ShortDateProduct("UNITNUMBER", null, null, null), "productCode cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new ShortDateProduct("UNITNUMBER", "PRODUCTCODE", null, null), "aboRh cannot be null");

        assertDoesNotThrow(() -> new ShortDateProduct("UNITNUMBER", "PRODUCTCODE", "ABORH", null));
    }

}
