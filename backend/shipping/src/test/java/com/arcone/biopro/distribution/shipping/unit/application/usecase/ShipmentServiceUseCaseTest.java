package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.usecase.ShipmentServiceUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCreatedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderItemFulfilledMessage;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShortDateItem;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
class ShipmentServiceUseCaseTest {

    private ShipmentRepository shipmentRepository;
    private ShipmentItemRepository shipmentItemRepository;

    private ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;

    private InventoryRsocketClient inventoryRsocketClient;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private ReactiveKafkaProducerTemplate<String, ShipmentCompletedEvent> producerTemplate;
    private ApplicationEventPublisher applicationEventPublisher;

    private ShipmentServiceUseCase useCase;

    @BeforeEach
    public void setUp(){
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);
        inventoryRsocketClient = Mockito.mock(InventoryRsocketClient.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository,inventoryRsocketClient,shipmentItemPackedRepository,applicationEventPublisher);
    }
    @Test
    public void shouldCreateShipment(){

        OrderFulfilledMessage message = Mockito.mock(OrderFulfilledMessage.class);
        Mockito.when(message.orderNumber()).thenReturn(56L);
        Mockito.when(message.status()).thenReturn("OPEN");
        Mockito.when(message.priority()).thenReturn("ASAP");

        OrderItemFulfilledMessage item = Mockito.mock(OrderItemFulfilledMessage.class);
        Mockito.when(item.bloodType()).thenReturn("AP");
        List<OrderItemFulfilledMessage> items = new ArrayList<>();
        items.add(item);

        ShortDateItem shortDateItem = Mockito.mock(ShortDateItem.class);
        Mockito.when(shortDateItem.productCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.unitNumber()).thenReturn("UNIT_NUMBER");
        List<ShortDateItem> shortDateItems = new ArrayList<>();
        shortDateItems.add(shortDateItem);

        Mockito.when(item.shortDateProducts()).thenReturn(shortDateItems);


        Mockito.when(message.items()).thenReturn(items);

        Shipment shipment = Mockito.mock(Shipment.class);

        Mockito.when(shipmentRepository.save(Mockito.any(Shipment.class))).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemRepository.save(Mockito.any(ShipmentItem.class))).thenReturn(Mono.just(ShipmentItem.builder().id(1L).build()));

        Mockito.when(shipmentItemShortDateProductRepository.save(Mockito.any(ShipmentItemShortDateProduct.class))).thenReturn(Mono.just(ShipmentItemShortDateProduct.builder().id(1L).build()));

        Mono<Shipment> shipmentMono = useCase.create(message);

        StepVerifier.create(shipmentMono)
            .consumeNextWith(orderSaved -> orderSaved.getId())
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(ShipmentCreatedEvent.class));

    }

    @Test
    public void shouldListShipments(){

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);

        Mockito.when(shipmentRepository.findAll()).thenReturn(Flux.just(shipment));

        StepVerifier
            .create(useCase.listShipments())
            .expectNextCount(1L)
            .verifyComplete();
    }

    @Test
    public void shouldFindShipmentById(){



        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));


        ShipmentItem item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("product_family");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);

        Mockito.when(shipmentItemRepository.findAllByShipmentId(1L)).thenReturn(Flux.just(item));


        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(1L)).thenReturn(Flux.just(shortDateItem));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(Mockito.any())).thenReturn(Flux.just(ShipmentItemPacked.builder()
            .id(1L)
            .shipmentItemId(1L)
            .unitNumber("UN")
            .productCode("product_code")
            .build()));

        Mono<ShipmentDetailResponseDTO>  orderDetail = useCase.getShipmentById(1L);

        StepVerifier
            .create(orderDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(56L), Optional.of(detail.orderNumber()));
                assertEquals(Optional.of(ShipmentStatus.OPEN), Optional.of(detail.status()));
                assertEquals(Optional.of(ShipmentPriority.ASAP), Optional.of(detail.priority()));
                assertEquals(detail.items().size(), 1);
                assertEquals(Optional.of(BloodType.AP), Optional.of(detail.items().get(0).bloodType()));
                assertEquals(detail.items().get(0).shortDateProducts().size(), 1);
                assertEquals(Optional.of("ABCD"), Optional.of(detail.items().get(0).shortDateProducts().get(0).productCode()));
                assertEquals(Optional.of("UNIT_NUMBER"), Optional.of(detail.items().get(0).shortDateProducts().get(0).unitNumber()));
                assertEquals(detail.items().get(0).packedItems().size(), 1);
                assertEquals(Optional.of("product_code"), Optional.of(detail.items().get(0).packedItems().get(0).productCode()));
                assertEquals(Optional.of("UN"), Optional.of(detail.items().get(0).packedItems().get(0).unitNumber()));
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotPackItemWhenInventoryValidationFails(){

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryNotificationDTO()).thenReturn(InventoryNotificationDTO.builder()
                .errorMessage(ShipmentServiceMessages.INVENTORY_TEST_ERROR)
                .errorCode(1)
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .employeeId("test")
                .locationCode("MDL_HUB_1")
                .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of(ShipmentServiceMessages.INVENTORY_TEST_ERROR), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductCriteriaDoesNotMatch(){

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("MDL_HUB_1")
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .bloodType(BloodType.BP)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(10));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("MDL_HUB_1")
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of(ShipmentServiceMessages.PRODUCT_CRITERIA_BLOOD_TYPE_ERROR), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductCriteriaQuantityHasExceeded(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("MDL_HUB_1")
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .bloodType(BloodType.AP)
            .quantity(10)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(10));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));


        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("MDL_HUB_1")
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of(ShipmentServiceMessages.PRODUCT_CRITERIA_QUANTITY_ERROR), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductIsAlreadyFilled(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("MDL_HUB_1")
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .bloodType(BloodType.AP)
            .quantity(10)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(1));


        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("MDL_HUB_1")
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of(ShipmentServiceMessages.PRODUCT_ALREADY_USED_ERROR), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldPackItemWhenItIsSuitable(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("MDL_HUB_1")
                .productCode("123")
                .unitNumber("UN")
                .productFamily("product_family")
                .aboRh("AP")
            .build());

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .id(1L)
            .bloodType(BloodType.AP)
                .quantity(10)
            .build()));


        Mockito.when(shipmentItemPackedRepository.save(Mockito.any(ShipmentItemPacked.class))).thenReturn(Mono.just(ShipmentItemPacked.builder()
                .id(1L)
                .unitNumber("UN")
                .shipmentItemId(1L)
                .productCode("product_code")
                .packedByEmployeeId("test")
                .shipmentItemId(1L)
            .build()));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(ShipmentItemPacked.builder()
            .id(1L)
            .unitNumber("UN")
            .productCode("product_code")
            .packedByEmployeeId("test")
            .shipmentItemId(1L)
            .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(shortDateItem));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("MDL_HUB_1")
            .productCode("123")
                .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.OK), Optional.of(detail.ruleCode()));
                assertNull(detail.notifications());
                assertNotNull(detail.results());
                assertNotNull(detail.results().get("results"));
                assertNotNull(detail.results().get("results").get(0));
                ShipmentItemResponseDTO result = (ShipmentItemResponseDTO) detail.results().get("results").get(0);
                assertEquals(Optional.of("UN"), Optional.of(result.packedItems().get(0).unitNumber()));
                assertEquals(Optional.of("product_code"), Optional.of(result.packedItems().get(0).productCode()));
                assertEquals(Optional.of("test"), Optional.of(result.packedItems().get(0).packedByEmployeeId()));

                assertEquals(Optional.of("UNIT_NUMBER"), Optional.of(result.shortDateProducts().get(0).unitNumber()));
                assertEquals(Optional.of("ABCD"), Optional.of(result.shortDateProducts().get(0).productCode()));


            })
            .verifyComplete();
    }

    @Test
    public void shouldNotCompleteShipmentWhenDoesNotExist(){


        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
                .shipmentId(1L)
                .employeeId("test")
            .build());


        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotCompleteShipmentWhenItsAlreadyCompleted(){

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(Shipment.builder()
                .status(ShipmentStatus.COMPLETED)
            .build()));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(1L)).thenReturn(Flux.empty());

        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
            .shipmentId(1L)
            .employeeId("test")
            .build());


        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of(ShipmentServiceMessages.SHIPMENT_COMPLETED_ERROR), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();

    }

    @Test
    public void shouldCompleteShipment(){


        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(Shipment.builder()
            .status(ShipmentStatus.OPEN)
            .build()));

        Mockito.when(shipmentRepository.save(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder()
                .id(1L)
                .status(ShipmentStatus.COMPLETED)
            .build()));

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(1L)).thenReturn(Flux.just(ShipmentItemPacked.builder()
                .id(1L)
                .shipmentItemId(1L)
                .unitNumber("UN")
                .productCode("product_code")
            .build()));

        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
            .shipmentId(1L)
            .employeeId("test")
            .build());


        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.OK), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.OK.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("success"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of("/shipment/1/shipment-details"), Optional.of(detail._links().get("next")));
                assertEquals(Optional.of(ShipmentServiceMessages.SHIPMENT_COMPLETED_SUCCESS), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(ShipmentCompletedEvent.class));
    }

}
