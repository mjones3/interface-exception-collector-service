package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.application.mapper.SearchOrderCriteriaMapper;
import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = { SearchOrderCriteriaMapper.class })
class SearchOrderCriteriaMapperTest {

    @Autowired
    SearchOrderCriteriaMapper searchOrderCriteriaMapper;

    @Test
    void testMapToDTO() {
        // Setup
        var orderStatusOne = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        var statusList = List.of(orderStatusOne);
        var orderPriorityOne = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        var priorityList = List.of(orderPriorityOne);
        var customerOne = new OrderCustomerReport("name", "code");
        var customerList = List.of(customerOne);

        var searchOrderCriteria = new SearchOrderCriteria(statusList, priorityList, customerList);

        // Execute
        var result = searchOrderCriteriaMapper.mapToDTO(searchOrderCriteria);

        // Verify
        assertEquals(searchOrderCriteria.getOrderStatus().getFirst().getDescriptionKey(), result.orderStatus().getFirst().descriptionKey());
        assertEquals(searchOrderCriteria.getCustomers().getFirst().getCode(), result.customers().getFirst().code());
        assertEquals(searchOrderCriteria.getOrderPriorities().getFirst().getId().getOptionValue(), result.orderPriorities().getFirst().optionValue());
        assertEquals(1, result.orderPriorities().getFirst().orderNumber());
        assertTrue(result.orderStatus().getFirst().active());
    }

}
