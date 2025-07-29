package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ProductResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.VerifyProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.GetUnlabeledPackedItemsRequest;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.mapper.ProductResponseMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.GetUnlabeledPackedItemsUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUnlabeledPackedItemsUseCaseTest {

    @Mock
    private ShipmentItemPackedRepository shipmentItemPackedRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private SecondVerificationService secondVerificationService;

    private ProductResponseMapper productResponseMapper;
    private GetUnlabeledPackedItemsUseCase useCase;

    @BeforeEach
    void setUp() {
        productResponseMapper = Mappers.getMapper(ProductResponseMapper.class);
        useCase = new GetUnlabeledPackedItemsUseCase(
            shipmentItemPackedRepository,
            shipmentRepository,
            productResponseMapper,
            secondVerificationService
        );
    }

    @Test
    void shouldReturnUnlabeledPackedItemsSuccessfully() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        Shipment shipment = createValidShipment();
        ShipmentItemPacked packedItem = createPackedItem();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.just(shipment));
        when(shipmentItemPackedRepository.listAllPendingVerificationByShipmentIdAndUnitNumber(shipmentId, unitNumber))
            .thenReturn(Flux.just(packedItem));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.ruleCode());
                assertNotNull(response.results());
                assertTrue(response.results().containsKey("results"));

                List<?> results = response.results().get("results");
                assertNotNull(results);
                assertEquals(1, results.size());

                List<ProductResponseDTO> products = (List<ProductResponseDTO>) results.get(0);
                assertEquals(1, products.size());
                assertEquals("UNIT123", products.get(0).unitNumber());
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenShipmentNotFound() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.empty());
        when(secondVerificationService.getVerificationDetailsByShipmentId(shipmentId))
            .thenReturn(Mono.just(createVerifyProductResponseDTO()));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.ruleCode());
                assertNotNull(response.notifications());
                assertEquals(1, response.notifications().size());
                assertEquals(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR,
                    response.notifications().get(0).message());
                assertEquals(NotificationType.WARN.name(),
                    response.notifications().get(0).notificationType());
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenLabelStatusIsNotUnlabeled() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        Shipment shipment = createShipmentWithLabelStatus("LABELED");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.just(shipment));
        when(secondVerificationService.getVerificationDetailsByShipmentId(shipmentId))
            .thenReturn(Mono.just(createVerifyProductResponseDTO()));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.ruleCode());
                assertNotNull(response.notifications());
                assertEquals(1, response.notifications().size());
                assertEquals(ShipmentServiceMessages.SHIPMENT_LABEL_STATUS_ERROR,
                    response.notifications().get(0).message());
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenShipmentTypeIsNotInternalTransfer() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        Shipment shipment = createShipmentWithType("CUSTOMER");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.just(shipment));
        when(secondVerificationService.getVerificationDetailsByShipmentId(shipmentId))
            .thenReturn(Mono.just(createVerifyProductResponseDTO()));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.ruleCode());
                assertNotNull(response.notifications());
                assertEquals(1, response.notifications().size());
                assertEquals(ShipmentServiceMessages.SHIPMENT_TYPE_NOT_MATCH_ERROR,
                    response.notifications().get(0).message());
            })
            .verifyComplete();
    }

    @Test
    void shouldResetVerificationWhenNoPackedItemsFound() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        Shipment shipment = createValidShipment();
        ShipmentItemPacked resetItem = createPackedItem();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.just(shipment));
        when(shipmentItemPackedRepository.listAllPendingVerificationByShipmentIdAndUnitNumber(shipmentId, unitNumber))
            .thenReturn(Flux.empty());
        when(secondVerificationService.resetVerification(shipmentId, ShipmentServiceMessages.SECOND_VERIFICATION_UNIT_NOT_PACKED_ERROR))
            .thenReturn(Mono.just(resetItem));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.ruleCode());
                assertNotNull(response.results());
                assertTrue(response.results().containsKey("results"));
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleMultiplePackedItems() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        Shipment shipment = createValidShipment();
        ShipmentItemPacked item1 = createPackedItemWithDetails("UNIT123", "PROD123");
        ShipmentItemPacked item2 = createPackedItemWithDetails("UNIT456", "PROD456");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.just(shipment));
        when(shipmentItemPackedRepository.listAllPendingVerificationByShipmentIdAndUnitNumber(shipmentId, unitNumber))
            .thenReturn(Flux.just(item1, item2));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.ruleCode());
                assertNotNull(response.results());

                List<?> results = response.results().get("results");
                List<ProductResponseDTO> products = (List<ProductResponseDTO>) results.get(0);
                assertEquals(2, products.size());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryException() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        RuntimeException exception = new RuntimeException("Database error");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.error(exception));
        when(secondVerificationService.getVerificationDetailsByShipmentId(shipmentId))
            .thenReturn(Mono.just(createVerifyProductResponseDTO()));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.ruleCode());
                assertNotNull(response.notifications());
                assertEquals(1, response.notifications().size());
                assertEquals("Database error", response.notifications().get(0).message());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandlePackedItemsRepositoryException() {
        // Given
        Long shipmentId = 1L;
        String unitNumber = "UN123";
        GetUnlabeledPackedItemsRequest request = GetUnlabeledPackedItemsRequest.builder()
            .shipmentId(shipmentId)
            .unitNumber(unitNumber)
            .build();

        Shipment shipment = createValidShipment();
        RuntimeException exception = new RuntimeException("Repository error");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Mono.just(shipment));
        when(shipmentItemPackedRepository.listAllPendingVerificationByShipmentIdAndUnitNumber(shipmentId, unitNumber))
            .thenReturn(Flux.error(exception));
        when(secondVerificationService.getVerificationDetailsByShipmentId(shipmentId))
            .thenReturn(Mono.just(createVerifyProductResponseDTO()));

        // When & Then
        StepVerifier.create(useCase.getUnlabeledPackedItems(request))
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.ruleCode());
                assertNotNull(response.notifications());
                assertEquals("Repository error", response.notifications().get(0).message());
            })
            .verifyComplete();
    }

    private Shipment createValidShipment() {
        return Shipment.builder()
            .id(1L)
            .labelStatus("UNLABELED")
            .shipmentType("INTERNAL_TRANSFER")
            .build();
    }

    private Shipment createShipmentWithLabelStatus(String labelStatus) {
        return Shipment.builder()
            .id(1L)
            .labelStatus(labelStatus)
            .shipmentType("INTERNAL_TRANSFER")
            .build();
    }

    private Shipment createShipmentWithType(String shipmentType) {

        return Shipment.builder()
            .id(1L)
            .labelStatus("UNLABELED")
            .shipmentType(shipmentType)
            .build();
    }

    private ShipmentItemPacked createPackedItem() {
        return createPackedItemWithDetails("UNIT123", "PROD123");
    }

    private ShipmentItemPacked createPackedItemWithDetails(String unitNumber, String productCode) {
        return ShipmentItemPacked.builder()
            .id(1L)
        .unitNumber(unitNumber)
        .productCode(productCode)
        .productDescription("Test Product")
        .productStatus("AVAILABLE").build();

    }

    private VerifyProductResponseDTO createVerifyProductResponseDTO() {
        return VerifyProductResponseDTO.builder()
            .shipmentId(1L)
            .build();
    }
}
