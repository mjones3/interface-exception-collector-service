package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.application.mapper.PickListCommandMapper;
import com.arcone.biopro.distribution.order.application.mapper.PickListMapper;
import com.arcone.biopro.distribution.order.application.usecase.PickListUseCase;
import com.arcone.biopro.distribution.order.domain.event.PickListCreatedEvent;
import com.arcone.biopro.distribution.order.domain.model.AvailableInventory;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.service.InventoryService;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig
class PickListUseCaseTest {

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    OrderService orderService;

    @MockBean
    InventoryService inventoryService;

    @MockBean
    PickListMapper pickListMapper;

    @MockBean
    PickListCommandMapper pickListCommandMapper;

    @Test
    public void shouldGeneratePickList() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.just(order));

        var availableInventory = Mockito.mock(AvailableInventory.class);

        Mockito.when(inventoryService.getAvailableInventories(Mockito.any())).thenReturn(Flux.just(availableInventory));

        var pickList = Mockito.mock(PickList.class);
        Mockito.when(pickList.getOrderNumber()).thenReturn(1L);
        Mockito.when(pickList.getOrderStatus()).thenReturn("OPEN");

        Mockito.when(pickListMapper.mapToDomain(Mockito.any())).thenReturn(pickList);

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.getOrderNumber());

                }
            )
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(PickListCreatedEvent.class));
    }

    @Test
    public void shouldGeneratePickListWhenInventoryServiceIsDown() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.just(order));

        Mockito.when(inventoryService.getAvailableInventories(Mockito.any())).thenReturn(Flux.error(new Throwable("inventory-service-error")));

        var pickList = Mockito.mock(PickList.class);
        Mockito.when(pickList.getOrderNumber()).thenReturn(1L);

        Mockito.when(pickListMapper.mapToDomain(Mockito.any())).thenReturn(pickList);

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.getOrderNumber());

                }
            )
            .verifyComplete();
    }

    @Test
    public void shouldNotGeneratePickListWhenOrderNotFound() {
        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.error(new DomainNotFoundForKeyException("test")));

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L))
            .verifyError();

        Mockito.verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    public void shouldGeneratePickListAndNotGenerateEvenWhenOrderIsNotOpen() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.just(order));

        var availableInventory = Mockito.mock(AvailableInventory.class);

        Mockito.when(inventoryService.getAvailableInventories(Mockito.any())).thenReturn(Flux.just(availableInventory));

        var pickList = Mockito.mock(PickList.class);
        Mockito.when(pickList.getOrderNumber()).thenReturn(1L);
        Mockito.when(pickList.getOrderStatus()).thenReturn("IN_PROGRESS");

        Mockito.when(pickListMapper.mapToDomain(Mockito.any())).thenReturn(pickList);

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.getOrderNumber());

                }
            )
            .verifyComplete();

        Mockito.verifyNoInteractions(applicationEventPublisher);
    }
}
