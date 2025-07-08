package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.SearchOrderCriteriaController;
import com.arcone.biopro.distribution.order.application.mapper.SearchOrderCriteriaMapper;
import com.arcone.biopro.distribution.order.domain.model.Location;
import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.SearchOrderCriteria;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.SearchOrderCriteriaService;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


class SearchOrderCriteriaControllerTest {

    @Test
    public void shouldSearchOrderCriteria(){

        var customer = Mockito.mock(CustomerDTO.class);
        Mockito.when(customer.code()).thenReturn("code");
        Mockito.when(customer.name()).thenReturn("name");

        var lookupId = Mockito.mock(LookupId.class);
        Mockito.when(lookupId.getType()).thenReturn("type");
        Mockito.when(lookupId.getOptionValue()).thenReturn("value");

        var lookup = Mockito.mock(Lookup.class);
        Mockito.when(lookup.getId()).thenReturn(lookupId);
        Mockito.when(lookup.isActive()).thenReturn(true);

        var lookupServiceMock = Mockito.mock(LookupService.class);
        var customerServiceMock = Mockito.mock(CustomerService.class);
        var locationRepositoryMock = Mockito.mock(LocationRepository.class);

        Mockito.when(lookupServiceMock.findAllByType(Mockito.any())).thenReturn(Flux.just(lookup));

        Mockito.when(customerServiceMock.getCustomers()).thenReturn(Flux.just(customer));

        var locationMock = Mockito.mock(Location.class);
        Mockito.when(locationMock.getCode()).thenReturn("code");
        Mockito.when(locationMock.getName()).thenReturn("name");

        Mockito.when(locationRepositoryMock.findAll()).thenReturn(Flux.just(locationMock));


        var serviceMock = Mockito.mock(SearchOrderCriteriaService.class);

        var searchOrderCriteriaMock = new SearchOrderCriteria(lookupServiceMock, customerServiceMock,locationRepositoryMock);

        Mockito.when(serviceMock.searchOrderCriteria()).thenReturn(Mono.just(searchOrderCriteriaMock));


        SearchOrderCriteriaController searchOrderCriteriaController = new SearchOrderCriteriaController(serviceMock,new SearchOrderCriteriaMapper());


        StepVerifier.create(searchOrderCriteriaController.searchOrderCriteria())
            .expectNextMatches(searchOrderCriteria -> searchOrderCriteria.orderStatus().getFirst().type().equals("type"))
            .verifyComplete();
    }

}
