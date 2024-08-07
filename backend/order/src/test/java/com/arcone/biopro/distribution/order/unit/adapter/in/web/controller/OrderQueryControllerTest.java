package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.OrderQueryController;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderQueryCommandDTO;
import com.arcone.biopro.distribution.order.application.exception.QueryDidNotFindAnyResultsException;
import com.arcone.biopro.distribution.order.application.mapper.OrderQueryMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import com.arcone.biopro.distribution.order.domain.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;


class OrderQueryControllerTest {

    @Test
    public void shouldSearchOrders(){

        var serviceMock = Mockito.mock(OrderQueryService.class);
        var reportMock = Mockito.mock(OrderReport.class);
        var customerMock = Mockito.mock(OrderCustomerReport.class);
        var orderPriority = Mockito.mock(OrderPriorityReport.class);

        Mockito.when(reportMock.getOrderNumber()).thenReturn(1L);
        Mockito.when(reportMock.getOrderCustomerReport()).thenReturn(customerMock);
        Mockito.when(reportMock.getOrderPriorityReport()).thenReturn(orderPriority);

        Mockito.when(serviceMock.searchOrders(Mockito.any())).thenReturn(Flux.just(reportMock));


        OrderQueryController orderQueryController = new OrderQueryController(serviceMock,new OrderQueryMapper());

        OrderQueryCommandDTO command = OrderQueryCommandDTO
            .builder()
            .locationCode("1")
            .build();

        StepVerifier.create(orderQueryController.searchOrders(command))
            .expectNextMatches(orderReportDTO -> orderReportDTO.orderNumber().equals(1L))
            .verifyComplete();
    }

    @Test
    public void shouldNotSearchOrders(){

        var serviceMock = Mockito.mock(OrderQueryService.class);

        Mockito.when(serviceMock.searchOrders(Mockito.any())).thenReturn(Flux.error(new QueryDidNotFindAnyResultsException()));

        OrderQueryController orderQueryController = new OrderQueryController(serviceMock,new OrderQueryMapper());

        OrderQueryCommandDTO command = OrderQueryCommandDTO
            .builder()
            .locationCode("1")
            .build();

        StepVerifier.create(orderQueryController.searchOrders(command))
            .expectError(QueryDidNotFindAnyResultsException.class)
            .verify();

    }
}
