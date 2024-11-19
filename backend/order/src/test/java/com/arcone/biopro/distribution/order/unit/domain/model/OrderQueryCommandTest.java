package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderQueryCommandTest {

    @Test
    public void shouldCreateOrderQueryCommandWhenSortIsNull() {
        var orderQueryCommand = new OrderQueryCommand("1","",null,null,null,null,null,null,null,null,10);
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
        var orderQueryCommand = new OrderQueryCommand("1","",null,null,null,null,null,null,null,sort,10);
        Assertions.assertNotNull(orderQueryCommand);
        Assertions.assertNotNull(orderQueryCommand.getQuerySort());
        Assertions.assertNotNull(orderQueryCommand.getQuerySort().getQueryOrderByList());
        Assertions.assertEquals("DESC",orderQueryCommand.getQuerySort().getQueryOrderByList().get(0).getDirection());
        Assertions.assertEquals("TEST",orderQueryCommand.getQuerySort().getQueryOrderByList().get(0).getProperty());
    }

    @Test
    public void shouldNotCreateOrderQueryCommand() {
        assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand(null,null,null,null,null,null,null,null,null, null,null));

        Exception exception =  assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand(null,null,null,null,null,null,null,null,null, null,10));
        Assertions.assertEquals("locationCode cannot be null or empty", exception.getMessage());

        exception =  assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand("TEST",null,null,null,null,null,null,null,null, null,-1));
        Assertions.assertEquals("limit must be greater than 0", exception.getMessage());
    }

    @Test
    public void shouldCreateOrderQueryCommandWithNumericUniqueIdentifier() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));
        var uniqueIdentifier = "123";
        var orderQueryCommand = new OrderQueryCommand("1","123",null,null,null,null,null,null,null,sort,10);
        Assertions.assertEquals(uniqueIdentifier, orderQueryCommand.getOrderNumber());
        Assertions.assertEquals(uniqueIdentifier, orderQueryCommand.getExternalOrderId());
    }

    @Test
    public void shouldCreateOrderQueryCommandWithNonNumericUniqueIdentifier() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));
        var uniqueIdentifier = "F123";
        var orderQueryCommand = new OrderQueryCommand("1",uniqueIdentifier,null,null,null,null,null,null,null,sort,10);
        Assertions.assertEquals(uniqueIdentifier, orderQueryCommand.getExternalOrderId());
        Assertions.assertNull(orderQueryCommand.getOrderNumber());
    }

    @Test
    public void shouldThrowAnExceptionWhenInitialDateIsGreaterThanFinalDate() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));

        assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand("1",null,null,null,null, LocalDate.now().plusDays(1), LocalDate.now(),null,null,sort,10), "Initial date should not be greater than final date");
    }

    @Test
    public void shouldThrowAnExceptionWhenFinalDateIsGreaterThanToday() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));

        assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand("1",null,null,null,null, LocalDate.now().minusMonths(3), LocalDate.now().plusDays(1),null,null,sort,10), "Final date should not be greater than today");
    }

    @Test
    public void shouldThrowAnExceptionWhenDateRangeExceededTwoYears() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));

        assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand("1",null,null,null,null, LocalDate.now().minusYears(3), LocalDate.now().minusMonths(1),null,null,sort,10), "Date range exceeds two years");
    }

    @Test
    public void shouldThrowAnExceptionWhenOrderNUmberAndCreateDateHaveValues() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));
        var uniqueIdentifier = "F123";

        assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand("1",uniqueIdentifier,null,null,null, LocalDate.now().minusYears(1), LocalDate.now().minusMonths(1),null,null,sort,10), "The createDate must be null or empty");
    }

    @Test
    public void shouldThrowAnExceptionWhenOrderNumberAndCreateDateDoNotHaveValues() {
        var orderBy = new QueryOrderBy("TEST","DESC");
        var sort = new QuerySort(List.of(orderBy));

        assertThrows(IllegalArgumentException.class, () -> new OrderQueryCommand("1",null,null,null,null, null, null,null,null,sort,10), "The createDate must not be null or empty");
    }
}
