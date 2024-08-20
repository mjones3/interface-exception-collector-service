package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.application.mapper.OrderReceivedEventMapper;
import com.arcone.biopro.distribution.order.application.mapper.PickListCommandMapper;
import com.arcone.biopro.distribution.order.application.usecase.OrderUseCase;
import com.arcone.biopro.distribution.order.domain.model.AvailableInventory;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductFamily;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.InventoryService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

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

    @MockBean
    InventoryService inventoryService;

    @MockBean
    PickListCommandMapper pickListCommandMapper;

    @Test
    void testFindOneById() {

        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher,inventoryService, pickListCommandMapper);

        var orderMock = Mockito.mock(Order.class);

        Mockito.when(orderRepository.findOneById(anyLong())).thenReturn(Mono.just(orderMock));

        var availableInventory = Mockito.mock(AvailableInventory.class);

        Mockito.when(inventoryService.getAvailableInventories(Mockito.any())).thenReturn(Flux.just(availableInventory));


        StepVerifier.create(useCase.findOneById(1L))
            .expectNext(orderMock)
            .verifyComplete();
    }

    @Test
    void shouldNotFindOneById() {

        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher,inventoryService, pickListCommandMapper);

        Mockito.when(orderRepository.findOneById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findOneById(1L))
            .expectError(DomainNotFoundForKeyException.class)
            .verify();
    }

    @Test
    void shouldProcessOrder(){
        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher,inventoryService, pickListCommandMapper);

        var orderMock = Mockito.mock(Order.class);

        Mockito.when(orderRepository.insert(Mockito.any(Order.class))).thenReturn(Mono.just(orderMock));
        Mockito.when(eventMapper.mapToDomain(any())).thenReturn(Mono.just(orderMock));

        StepVerifier.create(useCase.processOrder(OrderReceivedEventPayloadDTO.builder().build()))
            .expectNext(orderMock)
            .verifyComplete();
    }

    @Test
    void shouldProcessOrderWhenValidationFails(){
        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher,inventoryService, pickListCommandMapper);

        Mockito.when(eventMapper.mapToDomain(any())).thenReturn(Mono.error(new IllegalArgumentException("TEST")));

        StepVerifier.create(useCase.processOrder(OrderReceivedEventPayloadDTO.builder().build()))
            .expectError()
            .verify();
    }

    @Test
    void shouldProcessOrderWhenDuplicatedOrder(){
        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher,inventoryService, pickListCommandMapper);

        var orderMock = Mockito.mock(Order.class);

        Mockito.when(eventMapper.mapToDomain(any())).thenReturn(Mono.just(orderMock));

        Mockito.when(orderRepository.insert(Mockito.any(Order.class))).thenThrow(DuplicateKeyException.class);

        StepVerifier.create(useCase.processOrder(OrderReceivedEventPayloadDTO.builder().build()))
            .expectError()
            .verify();
    }

    @Test
    void shouldSetAvailableInventories(){

        var useCase = new OrderUseCase(orderRepository, eventMapper,applicationEventPublisher,inventoryService, pickListCommandMapper);

        var orderMock = Mockito.mock(Order.class);

        var orderItem = Mockito.mock(OrderItem.class);

        var bloodType = Mockito.mock(BloodType.class);
        Mockito.when(bloodType.getBloodType()).thenReturn("AB");
        Mockito.when(orderItem.getBloodType()).thenReturn(bloodType);

        var productFamily = Mockito.mock(ProductFamily.class);
        Mockito.when(productFamily.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(orderItem.getProductFamily()).thenReturn(productFamily);

        Mockito.when(orderMock.getOrderItems()).thenReturn(List.of(orderItem));

        Mockito.when(orderRepository.findOneById(anyLong())).thenReturn(Mono.just(orderMock));

        var availableInventory = Mockito.mock(AvailableInventory.class);
        Mockito.when(availableInventory.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(availableInventory.getAboRh()).thenReturn("AB");

        Mockito.when(availableInventory.getQuantityAvailable()).thenReturn(10);

        Mockito.when(inventoryService.getAvailableInventories(Mockito.any())).thenReturn(Flux.just(availableInventory));

        StepVerifier.create(useCase.findOneById(1L))
            .expectNext(orderMock)
            .verifyComplete();

        Mockito.verify(orderItem).defineAvailableQuantity(10);
    }
}
