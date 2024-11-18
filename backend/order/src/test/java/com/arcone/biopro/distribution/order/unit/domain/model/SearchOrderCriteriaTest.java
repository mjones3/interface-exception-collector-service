package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchOrderCriteriaTest {


    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SearchOrderCriteria(null,null,null), "orderStatus are not valid");

        var orderStatusOne = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        var statusList = List.of(orderStatusOne);

        assertThrows(IllegalArgumentException.class, () -> new SearchOrderCriteria(statusList,null,null), "orderPriorities are not valid");

        var orderPriorityOne = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        var priorityList = List.of(orderPriorityOne);


        assertThrows(IllegalArgumentException.class, () -> new SearchOrderCriteria(statusList, priorityList,null), "customers are not valid");

        var customerOne = new OrderCustomerReport("name", "code");
        var customerList = List.of(customerOne);

        assertDoesNotThrow(() -> new SearchOrderCriteria(statusList,priorityList,customerList));

    }
}
