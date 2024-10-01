package com.arcone.biopro.distribution.shipping.unit.domain.vo;

import com.arcone.biopro.distribution.shipping.domain.model.vo.LookupId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LookupIdTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new LookupId(null, null));
        assertThrows(IllegalArgumentException.class, () -> new LookupId("type", null));
        assertThrows(IllegalArgumentException.class, () -> new LookupId(null, "description"));
        assertThrows(IllegalArgumentException.class, () -> new LookupId("", ""));
        assertThrows(IllegalArgumentException.class, () -> new LookupId("", "description"));
        assertThrows(IllegalArgumentException.class, () -> new LookupId("type", ""));
        assertDoesNotThrow(() -> new LookupId("type", "description"));
    }

}
