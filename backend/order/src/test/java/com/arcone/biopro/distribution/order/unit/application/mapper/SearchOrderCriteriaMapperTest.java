package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.application.mapper.SearchOrderCriteriaMapper;
import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = { SearchOrderCriteriaMapper.class })
class SearchOrderCriteriaMapperTest {

    @Autowired
    SearchOrderCriteriaMapper searchOrderCriteriaMapper;

    @MockBean
    LookupService lookupService;
    @MockBean
    CustomerService customerService;

    @Test
    void testMapToDTO() {
        // Setup
        var lookup = new Lookup(new LookupId("type", "value"),"description",1,true);

        var customer = new CustomerDTO("code","123","name","","","",null, "Y");

        Mockito.when(lookupService.findAllByType(Mockito.any())).thenReturn(Flux.just(lookup));

        Mockito.when(customerService.getCustomers()).thenReturn(Flux.just(customer));

        var searchOrderCriteria = new SearchOrderCriteria(lookupService, customerService);

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
