package com.arcone.biopro.distribution.order.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.infrastructure.mapper.SearchOrderCriteriaEntityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = { SearchOrderCriteriaEntityMapper.class })
class SearchOrderCriteriaEntityMapperTest {

    @Autowired
    SearchOrderCriteriaEntityMapper mapper;

    @Test
    public void testMapToDomain() {
        // Setup
        var orderStatusOne = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        var statusList = List.of(orderStatusOne);
        var orderPriorityOne = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        var priorityList = List.of(orderPriorityOne);
        var customerOne = new OrderCustomerReport("name", "code");
        var customerList = List.of(customerOne);

        // Execute
        var searchOrderCriteria = mapper.mapToDomain(statusList, priorityList, customerList);

        // Verify
        assertEquals(searchOrderCriteria.getOrderStatus(), statusList);
        assertEquals(searchOrderCriteria.getOrderStatus().getFirst().getId(), statusList.getFirst().getId());
        assertEquals(searchOrderCriteria.getCustomers().getFirst().getCode(), customerList.getFirst().getCode());
        assertEquals(searchOrderCriteria.getOrderPriorities().getFirst().getDescriptionKey(), priorityList.getFirst().getDescriptionKey());
        assertEquals(searchOrderCriteria.getOrderPriorities().getFirst().isActive(), priorityList.getFirst().isActive());
    }

}
