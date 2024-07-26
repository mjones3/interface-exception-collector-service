package com.arcone.biopro.distribution.orderservice.unit.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderExternalId;
import graphql.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderExternalIdTest {

    @Test
    public void shouldCreateOrderExternalId() {
        Assert.assertNotNull(new OrderExternalId("123"));
    }

    @Test
    public void shouldNotCreateOrderExternalIdWhenIdIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderExternalId(null));
        assertEquals("orderExternalId cannot be null or blank", exception.getMessage());

    }

    @Test
    public void shouldNotCreateOrderExternalIdWhenIdIsInvalidFormat() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderExternalId("0-@123"));
        assertEquals("orderExternalId is not a valid Format", exception.getMessage());
    }
}
