package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.QueryOrderBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryOrderByTest {

    @Test
    public void shouldCreateQueryOrderBy() {
        new QueryOrderBy("TEST", "ASC");
    }

    @Test
    public void shouldNotCreateQueryOrderBy() {

        var exception =  assertThrows(IllegalArgumentException.class, () ->  new QueryOrderBy(null, "ASC"));
        Assertions.assertEquals("Property must not be null", exception.getMessage());


        exception =  assertThrows(IllegalArgumentException.class, () ->  new QueryOrderBy("TEST", null));
        Assertions.assertEquals("Direction must not be null", exception.getMessage());

    }

    @Test
    public void shouldNotCreateQueryOrderByWhenDirectionIsInvalid() {

        var exception =  assertThrows(IllegalArgumentException.class, () ->  new QueryOrderBy("TEST", "TEST"));
        Assertions.assertEquals("Direction is invalid", exception.getMessage());

    }
}
