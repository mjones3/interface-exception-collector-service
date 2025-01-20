package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UnpackItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.UnpackItemsRequest;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.SecondVerificationUseCase;
import com.arcone.biopro.distribution.shipping.application.usecase.UnpackItemUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class UnpackItemUseCaseTest {

    private ShipmentItemRepository shipmentItemRepository;
    private ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private ShipmentMapper shipmentMapper;
    private UnpackItemUseCase useCase;
    private ShipmentRepository shipmentRepository;
    private SecondVerificationService secondVerificationService;


    @BeforeEach
    public void setUp() {
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);
        shipmentItemPackedRepository = Mockito.mock(ShipmentItemPackedRepository.class);
        shipmentMapper = new ShipmentMapper();
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        secondVerificationService = new SecondVerificationUseCase(shipmentItemPackedRepository, shipmentRepository, shipmentMapper);

        useCase = new UnpackItemUseCase(shipmentItemPackedRepository, shipmentItemRepository, shipmentItemShortDateProductRepository, shipmentMapper, shipmentRepository, secondVerificationService);
    }

    @Test
    public void shouldNotUnPackItemWhenShipmentItemDoesNotExist() {

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.empty());


        Mono<RuleResponseDTO> unpackDetail = useCase.unpackItems(UnpackItemsRequest.builder()
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .unpackItems(List.of(UnpackItemRequest.builder()
                .unitNumber("UN")
                .productCode("123")
                .build()))
            .build()
        );

        StepVerifier
            .create(unpackDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(ShipmentServiceMessages.UNPACK_SHIPMENT_ITEM_NOT_FOUND_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotUnPackItemWhenShipmentDoesNotExist() {

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .shipmentId(1L)
            .id(1L)
            .productFamily("product_family")
            .bloodType(BloodType.BP)
            .build()));

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.empty());

        Mono<RuleResponseDTO> unpackDetail = useCase.unpackItems(UnpackItemsRequest.builder()
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .unpackItems(List.of(UnpackItemRequest.builder()
                .unitNumber("UN")
                .productCode("123")
                .build()))
            .build()
        );

        StepVerifier
            .create(unpackDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(ShipmentServiceMessages.UNPACK_SHIPMENT_NOT_FOUND_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotUnPackItemWhenShipmentIsCompleted() {

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .shipmentId(1L)
            .id(1L)
            .productFamily("product_family")
            .bloodType(BloodType.BP)
            .build()));

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(Shipment.builder()
            .id(1L)
            .status(ShipmentStatus.COMPLETED)
            .build()));

        Mono<RuleResponseDTO> unpackDetail = useCase.unpackItems(UnpackItemsRequest.builder()
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .unpackItems(List.of(UnpackItemRequest.builder()
                .unitNumber("UN")
                .productCode("123")
                .build()))
            .build()
        );

        StepVerifier
            .create(unpackDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(ShipmentServiceMessages.UNPACK_SHIPMENT_COMPLETED_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }


    @Test
    public void shouldNotUnPackItemWhenProductIsNotPacked() {

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .shipmentId(1L)
            .id(1L)
            .productFamily("product_family")
            .bloodType(BloodType.BP)
            .build()));

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(Shipment.builder()
            .id(1L)
            .status(ShipmentStatus.OPEN)
            .build()));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(ShipmentItemPacked.builder()
            .id(1L)
            .unitNumber("UN")
            .productCode("product_code")
            .packedByEmployeeId("test")
            .shipmentItemId(1L)
            .build()));

        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(shortDateItem));

        Mockito.when(shipmentItemPackedRepository.findByShipmentIUnitNumberAndProductCode(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.empty());

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.empty());


        Mono<RuleResponseDTO> unpackDetail = useCase.unpackItems(UnpackItemsRequest.builder()
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .unpackItems(List.of(UnpackItemRequest.builder()
                .unitNumber("UN")
                .productCode("123")
                .build()))
            .build()
        );

        StepVerifier
            .create(unpackDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(ShipmentServiceMessages.UNPACK_PRODUCT_NOT_FOUND_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldUnpackItem() {

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .shipmentId(1L)
            .id(1L)
            .productFamily("product_family")
            .bloodType(BloodType.BP)
            .build()));

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(Shipment.builder()
            .id(1L)
            .status(ShipmentStatus.OPEN)
            .build()));

        Mockito.when(shipmentItemPackedRepository.findByShipmentIUnitNumberAndProductCode(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.just(ShipmentItemPacked
            .builder()
            .id(1L)
            .build()));

        Mockito.when(shipmentItemPackedRepository.delete(Mockito.any())).thenReturn(Mono.empty());


        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(ShipmentItemPacked.builder()
            .id(1L)
            .unitNumber("UN")
            .productCode("product_code")
            .packedByEmployeeId("test")
            .shipmentItemId(1L)
            .build()));

        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(shortDateItem));
        var packedItem = ShipmentItemPacked.builder().unitNumber("UN").productCode("123").build();
        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));
        Mockito.when(shipmentItemPackedRepository.save(Mockito.any())).thenReturn(Mono.just(packedItem));


        Mono<RuleResponseDTO> unpackDetail = useCase.unpackItems(UnpackItemsRequest.builder()
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .unpackItems(List.of(UnpackItemRequest.builder()
                .unitNumber("UN")
                .productCode("123")
                .build()))
            .build()
        );

        StepVerifier
            .create(unpackDetail)
            .consumeNextWith(detail -> {
                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertNotNull(detail.notifications());
                assertNotNull(detail.results());

                var firstNotification = detail.notifications().getFirst();

                assertEquals("success", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.UNPACK_ITEM_SUCCESS, firstNotification.message());

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var shipmentItem = (ShipmentItemResponseDTO) firstRuleResult;
                var firstPackedItem = shipmentItem.packedItems().getFirst();
                assertEquals("UN", firstPackedItem.unitNumber());
                assertEquals("product_code", firstPackedItem.productCode());
                assertEquals("test", firstPackedItem.packedByEmployeeId());

                var firstShortDateProduct = shipmentItem.shortDateProducts().getFirst();
                assertEquals("UNIT_NUMBER", firstShortDateProduct.unitNumber());
                assertEquals("ABCD", firstShortDateProduct.productCode());
            })
            .verifyComplete();

    }

}
