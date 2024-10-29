package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderQueryCommandTest {

    @Test
    public void shouldCreateOrderQueryCommandWhenSortIsNull() {
        var orderQueryCommand = new OrderQueryCommand("1","",null,10);
        Assertions.assertNotNull(orderQueryCommand);
        Assertions.assertNotNull(orderQueryCommand.getQuerySort());
        Assertions.assertNotNull(orderQueryCommand.getQuerySort().getQueryOrderByList());
        Assertions.assertEquals("ASC",orderQueryCommand.getQuerySort().getQueryOrderByList().get(0).getDirection());
        Assertions.assertEquals("priority",orderQueryCommand.getQuerySort().getQueryOrderByList().get(0).getProperty());
        Assertions.assertEquals("ASC",orderQueryCommand.getQuerySort().getQueryOrderByList().get(1).getDirection());
        Assertions.assertEquals("status",orderQueryCommand.getQuerySort().getQueryOrderByList().get(1).getProperty());
    }

    @Test
    public void shouldCreateOrderQueryCommand() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));
        var orderQueryCommand = new OrderQueryCommand("1","",sort,10);
        Assertions.assertNotNull(orderQueryCommand);
        Assertions.assertNotNull(orderQueryCommand.getQuerySort());
        Assertions.assertNotNull(orderQueryCommand.getQuerySort().getQueryOrderByList());
        Assertions.assertEquals("DESC",orderQueryCommand.getQuerySort().getQueryOrderByList().get(0).getDirection());
        Assertions.assertEquals("TEST",orderQueryCommand.getQuerySort().getQueryOrderByList().get(0).getProperty());
    }

    @Test
    public void shouldNotCreateOrderQueryCommand() {
        assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand(null,null, null,null));

        Exception exception =  assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand(null,null, null,10));
        Assertions.assertEquals("locationCode cannot be null or empty", exception.getMessage());

        exception =  assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand("TEST",null, null,-1));
        Assertions.assertEquals("limit must be greater than 0", exception.getMessage());
    }
}
