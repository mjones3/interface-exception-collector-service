package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.GetUnlabeledProductsRequest;
import com.arcone.biopro.distribution.shipping.application.mapper.ProductResponseMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.GetUnlabeledProductsUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import com.arcone.biopro.distribution.shipping.infrastructure.service.errors.InventoryServiceNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
class GetUnlabeledProductsUseCaseTest {

    private ShipmentItemRepository shipmentItemRepository;
    private InventoryRsocketClient inventoryRsocketClient;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private GetUnlabeledProductsUseCase useCase;
    private ShipmentRepository shipmentRepository;
    private ProductResponseMapper mapper;

    @BeforeEach
    public void setUp(){
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        inventoryRsocketClient = Mockito.mock(InventoryRsocketClient.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        mapper = Mappers.getMapper(ProductResponseMapper.class);

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.empty());

        useCase = new GetUnlabeledProductsUseCase(shipmentItemRepository,shipmentItemPackedRepository,inventoryRsocketClient,shipmentRepository,mapper);
    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenLabelStatusIsNotUnlabeled(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("LABELED");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                    .unitNumber("UN")
                    .shipmentItemId(1L)
                    .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(ShipmentServiceMessages.SHIPMENT_LABEL_STATUS_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenShipmentTypeIsNotInternalTransfer(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("CUSTOMER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(ShipmentServiceMessages.SHIPMENT_TYPE_NOT_MATCH_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenInventoryServiceIsDown(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenThrow(new InventoryServiceNotAvailableException("Inventory Error"));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("SYSTEM", firstNotification.notificationType());
                assertEquals("INVENTORY_SERVICE_IS_DOWN", firstNotification.name());
                assertEquals("Inventory Error", firstNotification.message());

            })
            .verifyComplete();

    }

    @Test
    public void shouldGetUnlabeledProducts(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                        .errorName("INVENTORY_IS_UNLABELED")
                    .build()))
                .inventoryResponseDTO(InventoryResponseDTO.builder()
                    .status("AVAILABLE")
                    .productCode("PRODUCT_CODE")
                    .unitNumber("UNIT_NUMBER")
                    .productFamily("PRODUCT_FAMILY")
                    .aboRh("AP")
                    .temperatureCategory("FROZEN")
                    .productDescription("PRODUCT_DESCRIPTION")
                    .isLabeled(false)
                    .build())
            .build()));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications();
                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertNull(firstNotification);

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var results = (List<ProductResponseDTO>) firstRuleResult;
                var firstProduct = results.getFirst();
                assertEquals("UNIT_NUMBER", firstProduct.unitNumber());
                assertEquals("PRODUCT_CODE", firstProduct.productCode());
                assertEquals("PRODUCT_DESCRIPTION", firstProduct.productDescription());
                assertEquals("AVAILABLE", firstProduct.status());

            })
            .verifyComplete();

    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenLabeledProducts(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                .errorName("INVENTORY_IS_UNLABELED")
                .build()))
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE")
                .unitNumber("UNIT_NUMBER")
                .productFamily("PRODUCT_FAMILY")
                .aboRh("AP")
                .temperatureCategory("FROZEN")
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(true)
                .build())
            .build()));


        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("INVENTORY_LABELED_ERROR", firstNotification.name());
                assertEquals(ShipmentServiceMessages.INVENTORY_LABELED_ERROR, firstNotification.message());

            })
            .verifyComplete();



    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenOrderCriteriaDoesNotMatch(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("REFRIGERATED");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                .errorName("INVENTORY_IS_UNLABELED")
                .build()))
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE")
                .unitNumber("UNIT_NUMBER")
                .productFamily("PRODUCT_FAMILY")
                .aboRh("AP")
                .temperatureCategory("FROZEN")
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(false)
                .build())
            .build()));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("ORDER_CRITERIA_DOES_NOT_MATCH_ERROR", firstNotification.name());
                assertEquals(ShipmentServiceMessages.ORDER_CRITERIA_DOES_NOT_MATCH_ERROR, firstNotification.message());

            })
            .verifyComplete();

    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenUnitDoesNotExist(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.empty());

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("INVENTORY_LABELED_ERROR", firstNotification.name());
                assertEquals(ShipmentServiceMessages.INVENTORY_LABELED_ERROR, firstNotification.message());

            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenUnitIsLabeled(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(Collections.emptyList())
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE")
                .unitNumber("UNIT_NUMBER")
                .productFamily("PRODUCT_FAMILY")
                .aboRh("AP")
                .temperatureCategory("FROZEN")
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(true)
                .build())
            .build()));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("INVENTORY_LABELED_ERROR", firstNotification.name());
                assertEquals(ShipmentServiceMessages.INVENTORY_LABELED_ERROR, firstNotification.message());

            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenUnitIsLabeledAndQuarantined(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getQuarantinedProducts()).thenReturn(true);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                .errorName("INVENTORY_IS_QUARANTINED")
                .build()))
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE")
                .unitNumber("UNIT_NUMBER")
                .productFamily("PRODUCT_FAMILY")
                .aboRh("AP")
                .temperatureCategory("FROZEN")
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(true)
                .build())
            .build()));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("INVENTORY_LABELED_ERROR", firstNotification.name());
                assertEquals(ShipmentServiceMessages.INVENTORY_LABELED_ERROR, firstNotification.message());

            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenProductIsAlreadyPacked(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        var packedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(packedItem.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(packedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                .errorName("INVENTORY_IS_UNLABELED")
                .build()))
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE")
                .unitNumber("UNIT_NUMBER")
                .productFamily("PRODUCT_FAMILY")
                .aboRh("AP")
                .temperatureCategory("FROZEN")
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(false)
                .build())
            .build()));



        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("CAUTION", firstNotification.notificationType());
                assertEquals("ALL_PRODUCTS_SELECTED_ERROR", firstNotification.name());
                assertEquals(ShipmentServiceMessages.ALL_PRODUCTS_SELECTED_ERROR, firstNotification.message());

            })
            .verifyComplete();
    }

    @Test
    public void shouldNotReturnUnlabeledProductsThatIsAlreadyPacked(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        var packedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(packedItem.getProductCode()).thenReturn("PRODUCT_CODE1");
        Mockito.when(packedItem.getUnitNumber()).thenReturn("UNIT_NUMBER1");

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                .errorName("INVENTORY_IS_UNLABELED")
                .build()))
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE2")
                .unitNumber("UNIT_NUMBER2")
                .productFamily("PRODUCT_FAMILY")
                .aboRh("AP")
                .temperatureCategory("FROZEN")
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(false)
                .build())
            .build()));


        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications();
                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertNull(firstNotification);

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var results = (List<ProductResponseDTO>) firstRuleResult;
                var firstProduct = results.getFirst();
                assertEquals(1, results.size());
                assertEquals("UNIT_NUMBER2", firstProduct.unitNumber());
                assertEquals("PRODUCT_CODE2", firstProduct.productCode());
                assertEquals("PRODUCT_DESCRIPTION", firstProduct.productDescription());
                assertEquals("AVAILABLE", firstProduct.status());

            })
            .verifyComplete();

    }

    @Test
    public void shouldNotReturnMultipleNotificationsWhenUnlabeledProducts(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        var packedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(packedItem.getProductCode()).thenReturn("PRODUCT_CODE1");
        Mockito.when(packedItem.getUnitNumber()).thenReturn("UNIT_NUMBER1");

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                .errorName("INVENTORY_IS_UNLABELED")
                    .errorType("WARN")
                    .errorMessage("Test Message")
                .errorCode(6)
                .build(),InventoryNotificationDTO
                .builder()
                    .errorName("INVENTORY_IS_PACKED")
                .build()))
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE2")
                .unitNumber("UNIT_NUMBER2")
                .productFamily("PRODUCT_FAMILY")
                .aboRh("AP")
                .temperatureCategory("FROZEN")
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(false)
                .build())
            .build()));


        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var notifications = detail.notifications();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertNotNull(notifications);
                assertEquals(1,notifications.size());

                var firstNotification = notifications.getFirst();

                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("INVENTORY_IS_UNLABELED", firstNotification.name());
                assertEquals("Test Message", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotGetUnlabeledProductsWhenOrderCriteriaDoesNotMatchInventoryDetailsNull(){

        var item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getShipmentId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.ANY);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("REFRIGERATED");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(item));

        Mockito.when(inventoryRsocketClient.validateInventoryByUnitNumber(Mockito.any())).thenReturn(Flux.just(InventoryValidationResponseDTO
            .builder()
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO.builder()
                .errorName("INVENTORY_IS_UNLABELED")
                .build()))
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .status("AVAILABLE")
                .productCode("PRODUCT_CODE")
                .unitNumber("UNIT_NUMBER")
                .productFamily(null)
                .aboRh("AP")
                .temperatureCategory(null)
                .productDescription("PRODUCT_DESCRIPTION")
                .isLabeled(false)
                .build())
            .build()));

        StepVerifier
            .create(useCase.getUnlabeledProducts(GetUnlabeledProductsRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .locationCode("LOCATION_CODE")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("ORDER_CRITERIA_DOES_NOT_MATCH_ERROR", firstNotification.name());
                assertEquals(ShipmentServiceMessages.ORDER_CRITERIA_DOES_NOT_MATCH_ERROR, firstNotification.message());

            })
            .verifyComplete();

    }
}
