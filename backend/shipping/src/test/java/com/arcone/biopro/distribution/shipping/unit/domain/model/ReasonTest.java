package com.arcone.biopro.distribution.shipping.unit.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReasonTest {

    @Test
    void shouldCreateReason() {

        new Reason(1L, "TYPE", "KEY", Boolean.FALSE, 1, Boolean.TRUE);
    }

    @Test
    void shouldNotCreateReason() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Reason(null, "TYPE", "KEY", Boolean.FALSE, 1, Boolean.TRUE), "ID cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Reason(1L, null, "KEY", Boolean.FALSE, 1, Boolean.TRUE), "type cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Reason(1L, "TYPE", null, Boolean.FALSE, 1, Boolean.TRUE), "reasonKey cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Reason(1L, "TYPE", "KEY", Boolean.FALSE, null, Boolean.TRUE), "orderNumber cannot be null");

    }

}
