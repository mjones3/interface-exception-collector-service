package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedPayloadDTO;
import com.arcone.biopro.distribution.order.application.mapper.ModifyOrderReceivedEventMapper;
import com.arcone.biopro.distribution.order.application.usecase.ModifyOrderUseCase;
import com.arcone.biopro.distribution.order.domain.event.OrderModifiedEvent;
import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.domain.model.ModifyOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig
class ModifyOrderUseCaseTest {

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    private OrderRepository orderRepository;
    private ModifyOrderUseCase useCase;
    private CustomerService customerService;
    private LookupService lookupService;
    private OrderConfigService orderConfigService;
    private ModifyOrderReceivedEventMapper modifyOrderReceivedEventMapper;
    private LocationRepository locationRepository;

    @BeforeEach
    public void setUp(){
        orderRepository = Mockito.mock(OrderRepository.class);
        customerService = Mockito.mock(CustomerService.class);
        lookupService = Mockito.mock(LookupService.class);
        orderConfigService = Mockito.mock(OrderConfigService.class);
        modifyOrderReceivedEventMapper = new ModifyOrderReceivedEventMapper();

        useCase = new ModifyOrderUseCase(orderRepository,modifyOrderReceivedEventMapper, applicationEventPublisher,customerService,lookupService,orderConfigService,locationRepository);
    }

    @Test
    void shouldProcessModifyOrderAndRejectWhenOrderDoesNotExist(){

        Mockito.when(orderRepository.findByExternalId(Mockito.any(String.class))).thenReturn(Flux.empty());

        var event = Mockito.mock(ModifyOrderReceivedDTO.class);
        Mockito.when(event.payload()).thenReturn(ModifyOrderReceivedPayloadDTO.builder()
            .externalId("externalId")
            .build());

        StepVerifier.create(useCase.processModifyOrderEvent(event))
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderRejectedEvent.class));
    }

    @Test
    void shouldProcessCancelOrderAndRejectWhenModifyOrderRequestIsInvalid(){

        var order = Mockito.mock(Order.class);
        Mockito.doThrow(new IllegalArgumentException("Invalid Request")).when(order).modify(Mockito.any(ModifyOrderCommand.class),Mockito.anyList(),Mockito.any(CustomerService.class),Mockito.any(),Mockito.any(),Mockito.any());

        Mockito.when(orderRepository.findByExternalId(Mockito.any(String.class))).thenReturn(Flux.just(order));

        var event = Mockito.mock(ModifyOrderReceivedDTO.class);
        Mockito.when(event.payload()).thenReturn(ModifyOrderReceivedPayloadDTO.builder()
            .externalId("externalId")
            .build());


        StepVerifier.create(useCase.processModifyOrderEvent(event))
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderRejectedEvent.class));
    }

    @Test
    void shouldProcessModifyOrder(){

        var order = Mockito.mock(Order.class);
        Mockito.when(order.modify(Mockito.any(ModifyOrderCommand.class),Mockito.anyList(),Mockito.any(CustomerService.class),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(order);

        var event = Mockito.mock(ModifyOrderReceivedDTO.class);
        Mockito.when(event.payload()).thenReturn(ModifyOrderReceivedPayloadDTO.builder()
            .externalId("externalId")
            .build());

        Mockito.when(orderRepository.findByExternalId(Mockito.any(String.class))).thenReturn(Flux.just(order));

        Mockito.when(orderRepository.reset(Mockito.any(Order.class))).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.processModifyOrderEvent(event))
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderModifiedEvent.class));
    }

}
