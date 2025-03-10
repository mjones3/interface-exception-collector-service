package com.arcone.biopro.distribution.order.unit.domain.vo;

import com.arcone.biopro.distribution.order.domain.model.Page;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PageTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Page<>(null, -1, -1, -1, null));
        assertThrows(IllegalArgumentException.class, () -> new Page<>(emptyList(), -1, -1, -1, null));
        assertThrows(IllegalArgumentException.class, () -> new Page<>(emptyList(), 0, -1, -1, null));
        assertThrows(IllegalArgumentException.class, () -> new Page<>(emptyList(), 0, 0, -1, null));
        assertDoesNotThrow(() -> new Page<>(emptyList(), 0, 0, 0, null));
    }

}
