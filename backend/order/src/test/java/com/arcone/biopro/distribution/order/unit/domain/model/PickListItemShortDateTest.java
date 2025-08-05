package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.PickListItemShortDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PickListItemShortDateTest {

    @Test
    void testValidatable() {
        assertThrows(IllegalArgumentException.class, () -> new PickListItemShortDate(null, null, null, null), "unitNumber cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new PickListItemShortDate("UNITNUMBER", null, null, null), "productCode cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new PickListItemShortDate("UNITNUMBER", "PRODUCTCODE", null, null), "aboRh cannot be null");

        assertDoesNotThrow(() -> new PickListItemShortDate("UNITNUMBER", "PRODUCTCODE", "ABORH", null));
    }

}
