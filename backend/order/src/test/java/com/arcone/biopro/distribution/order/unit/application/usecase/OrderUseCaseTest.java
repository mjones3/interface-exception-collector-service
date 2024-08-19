package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.application.mapper.OrderReceivedEventMapper;
import com.arcone.biopro.distribution.order.application.usecase.OrderUseCase;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@SpringJUnitConfig
public class OrderUseCaseTest {

    @MockBean
    CustomerService customerService;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    OrderConfigService orderConfigService;

    @MockBean
    LookupService lookupService;

    @MockBean
    OrderReceivedEventMapper eventMapper;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @Test
    void testFindOneById() {

        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher);

        var orderMock = Mockito.mock(Order.class);

        Mockito.when(orderRepository.findOneById(anyLong())).thenReturn(Mono.just(orderMock));

        StepVerifier.create(useCase.findOneById(1L))
            .expectNext(orderMock)
            .verifyComplete();
    }

    @Test
    void shouldNotFindOneById() {

        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher);

        Mockito.when(orderRepository.findOneById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findOneById(1L))
            .expectError(DomainNotFoundForKeyException.class)
            .verify();
    }

    @Test
    void shouldProcessOrder(){
        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher);

        var orderMock = Mockito.mock(Order.class);

        Mockito.when(orderRepository.insert(Mockito.any(Order.class))).thenReturn(Mono.just(orderMock));
        Mockito.when(eventMapper.mapToDomain(any())).thenReturn(Mono.just(orderMock));

        StepVerifier.create(useCase.processOrder(OrderReceivedEventPayloadDTO.builder().build()))
            .expectNext(orderMock)
            .verifyComplete();
    }

    @Test
    void shouldProcessOrderWhenValidationFails(){
        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher);

        Mockito.when(eventMapper.mapToDomain(any())).thenReturn(Mono.error(new IllegalArgumentException("TEST")));

        StepVerifier.create(useCase.processOrder(OrderReceivedEventPayloadDTO.builder().build()))
            .expectError()
            .verify();
    }

    @Test
    void shouldProcessOrderWhenDuplicatedOrder(){
        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher);

        var orderMock = Mockito.mock(Order.class);

        Mockito.when(eventMapper.mapToDomain(any())).thenReturn(Mono.just(orderMock));

        Mockito.when(orderRepository.insert(Mockito.any(Order.class))).thenThrow(DuplicateKeyException.class);

        StepVerifier.create(useCase.processOrder(OrderReceivedEventPayloadDTO.builder().build()))
            .expectError()
            .verify();
    }
}
