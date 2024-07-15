package com.arcone.biopro.distribution.orderservice.unit.domain;

import com.arcone.biopro.distribution.orderservice.domain.model.LookupId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LookupIdTest {

    @Test
    void testValidation() {
        assertFalse(new LookupId(null, null).isValid());
        assertFalse(new LookupId("type", null).isValid());
        assertFalse(new LookupId(null, "description").isValid());
        assertFalse(new LookupId("", "").isValid());
        assertFalse(new LookupId("", "description").isValid());
        assertFalse(new LookupId("type", "").isValid());
        assertTrue(new LookupId("type", "description").isValid());
    }

}
