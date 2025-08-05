package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
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

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);
        Mockito.when(useCaseResponse.data()).thenReturn(pickList);

        Mockito.when(pickListMapper.mapToUseCaseResponse(Mockito.any())).thenReturn(useCaseResponse);

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L,Boolean.FALSE))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.data().getOrderNumber());

                }
            )
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(PickListCreatedEvent.class));
    }

    @Test
    public void shouldGeneratePickListWhenInventoryServiceIsDownAndSkipServiceError() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.just(order));

        Mockito.when(inventoryService.getAvailableInventories(Mockito.any())).thenReturn(Flux.error(new Throwable("inventory-service-error")));

        var pickList = Mockito.mock(PickList.class);
        Mockito.when(pickList.getOrderNumber()).thenReturn(1L);

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);
        Mockito.when(useCaseResponse.data()).thenReturn(pickList);

        Mockito.when(pickListMapper.mapToUseCaseResponse(Mockito.any())).thenReturn(useCaseResponse);

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L,Boolean.TRUE))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.data().getOrderNumber());

                }
            )
            .verifyComplete();
    }

    @Test
    public void shouldNotGeneratePickListWhenOrderNotFound() {
        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.error(new DomainNotFoundForKeyException("test")));

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L,Boolean.FALSE))
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

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);
        Mockito.when(useCaseResponse.data()).thenReturn(pickList);

        Mockito.when(pickListMapper.mapToUseCaseResponse(Mockito.any())).thenReturn(useCaseResponse);


        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L,Boolean.FALSE))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.data().getOrderNumber());

                }
            )
            .verifyComplete();

        Mockito.verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    public void shouldNotGeneratePickListWhenInventoryServiceIsDownAndNotSkipServiceError() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.just(order));

        Mockito.when(inventoryService.getAvailableInventories(Mockito.any())).thenReturn(Flux.error(new Throwable("inventory-service-error")));

        var pickList = Mockito.mock(PickList.class);
        Mockito.when(pickList.getOrderNumber()).thenReturn(1L);

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);
        Mockito.when(useCaseResponse.data()).thenReturn(pickList);

        Mockito.when(pickListMapper.mapToUseCaseResponse(Mockito.any())).thenReturn(useCaseResponse);


        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L,Boolean.FALSE))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals("INVENTORY_SERVICE_IS_DOWN",  detail.notifications().getFirst().useCaseMessageType().name());
                Assertions.assertEquals("ERROR",  detail.notifications().getFirst().useCaseMessageType().getType().name());
                Assertions.assertEquals("Inventory Service is down.",  detail.notifications().getFirst().useCaseMessageType().getMessage());

                }
            )
            .verifyComplete();

        Mockito.verifyNoInteractions(applicationEventPublisher);
    }


    @Test
    public void shouldGeneratePickListWithoutAvailableInventories() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderService.findOneById(Mockito.eq(1L))).thenReturn(Mono.just(order));

        var pickList = Mockito.mock(PickList.class);
        Mockito.when(pickList.getOrderNumber()).thenReturn(1L);
        Mockito.when(pickList.getOrderStatus()).thenReturn("OPEN");
        Mockito.when(pickList.getShipmentType()).thenReturn("INTERNAL_TRANSFER");

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);
        Mockito.when(useCaseResponse.data()).thenReturn(pickList);

        Mockito.when(pickListMapper.mapToUseCaseResponse(Mockito.any())).thenReturn(useCaseResponse);

        var useCase = new PickListUseCase(applicationEventPublisher, orderService, inventoryService, pickListMapper, pickListCommandMapper);

        StepVerifier.create(useCase.generatePickList(1L,Boolean.FALSE))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.data().getOrderNumber());

                }
            )
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(PickListCreatedEvent.class));
        Mockito.verifyNoInteractions(inventoryService);
    }
}
