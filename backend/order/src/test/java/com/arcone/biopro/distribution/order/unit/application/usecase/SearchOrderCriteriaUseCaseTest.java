package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.usecase.SearchOrderCriteriaUseCase;
import com.arcone.biopro.distribution.order.domain.model.Location;
import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
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

    @MockBean
    LocationRepository locationRepository;


    @Test
    void testSearchOrderCriteria() {

        var useCase = new SearchOrderCriteriaUseCase(lookupService, customerService,locationRepository);

        var customer = Mockito.mock(CustomerDTO.class);
        Mockito.when(customer.code()).thenReturn("code");
        Mockito.when(customer.name()).thenReturn("name");

        var lookupId = Mockito.mock(LookupId.class);
        Mockito.when(lookupId.getType()).thenReturn("type");
        Mockito.when(lookupId.getOptionValue()).thenReturn("value");

        var lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(lookupId);

        Mockito.when(lookupService.findAllByType(Mockito.any())).thenReturn(Flux.just(lookup));

        Mockito.when(customerService.getCustomers()).thenReturn(Flux.just(customer));

        var locationMock = Mockito.mock(Location.class);
        Mockito.when(locationMock.getCode()).thenReturn("code");
        Mockito.when(locationMock.getName()).thenReturn("name");

        Mockito.when(locationRepository.findAll()).thenReturn(Flux.just(locationMock));

        var searchOrderCriteriaMock = new SearchOrderCriteria(lookupService, customerService,locationRepository);

        StepVerifier.create(useCase.searchOrderCriteria())
            .expectNext(searchOrderCriteriaMock)
            .verifyComplete();
    }

}
