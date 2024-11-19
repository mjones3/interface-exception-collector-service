package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.usecase.SearchOrderCriteriaUseCase;
import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringJUnitConfig
public class SearchOrderCriteriaUseCaseTest {

    @MockBean
    CustomerService customerService;

    @MockBean
    LookupService lookupService;


    @Test
    void testSearchOrderCriteria() {

        var useCase = new SearchOrderCriteriaUseCase(lookupService, customerService);

        var lookup = new Lookup(new LookupId("type", "value"),"description",1,true);

        var customer = new CustomerDTO("code","123","name","","","",null, "Y");

        Mockito.when(lookupService.findAllByType(Mockito.any())).thenReturn(Flux.just(lookup));

        Mockito.when(customerService.getCustomers()).thenReturn(Flux.just(customer));

        var searchOrderCriteriaMock = new SearchOrderCriteria(lookupService, customerService);

        StepVerifier.create(useCase.searchOrderCriteria())
            .expectNext(searchOrderCriteriaMock)
            .verifyComplete();
    }

}
