package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QuerySort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QuerySortTest {

    @Test
    public void shouldCreateQuerySort() {
        new QuerySort(List.of(new QueryOrderBy("TEST","ASC")));
    }

    @Test
    public void shouldNotCreateQuerySort() {

        var exception =  assertThrows(IllegalArgumentException.class, () ->  new QuerySort(null));
        Assertions.assertEquals("QueryOrderByList must not be null", exception.getMessage());


        exception =  assertThrows(IllegalArgumentException.class, () ->  new QuerySort(Collections.emptyList()));
        Assertions.assertEquals("QueryOrderByList must not be empty", exception.getMessage());

    }
}
