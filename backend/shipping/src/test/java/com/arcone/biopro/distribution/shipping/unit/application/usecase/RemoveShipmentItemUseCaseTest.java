package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.RemoveProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RemoveItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.RemoveShipmentItemUseCase;
import com.arcone.biopro.distribution.shipping.application.usecase.SecondVerificationUseCase;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemRemoved;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.IneligibleStatus;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRemovedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class RemoveShipmentItemUseCaseTest {

    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private ShipmentItemRemovedRepository shipmentItemRemovedRepository;
    private ShipmentMapper shipmentMapper;
    private RemoveShipmentItemUseCase useCase;
    private SecondVerificationService secondVerificationService;
    private ShipmentRepository shipmentRepository;

    @BeforeEach
    public void setup() {
        shipmentItemRemovedRepository = Mockito.mock(ShipmentItemRemovedRepository.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
        shipmentMapper = new ShipmentMapper();
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        secondVerificationService = new SecondVerificationUseCase(shipmentItemPackedRepository,shipmentRepository,shipmentMapper);
        useCase = new RemoveShipmentItemUseCase(shipmentItemPackedRepository,shipmentMapper,secondVerificationService,shipmentItemRemovedRepository);
    }

    @Test
    public void shouldNotRemoveShipmentItemWhenUnitNotFound() {

        Mockito.when(shipmentItemPackedRepository.findByUnitTobeRemoved(1L,"UN","123")).thenReturn(Mono.empty());

        var tobeRemovedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(tobeRemovedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(tobeRemovedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(tobeRemovedItem.getSecondVerification()).thenReturn(SecondVerification.COMPLETED);
        Mockito.when(tobeRemovedItem.getVerifiedByEmployeeId()).thenReturn("VERIFY_EMPLOYEE_ID");
        Mockito.when(tobeRemovedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");
        Mockito.when(tobeRemovedItem.getIneligibleStatus()).thenReturn(IneligibleStatus.INVENTORY_IS_EXPIRED);

        var removedItem = Mockito.mock(ShipmentItemRemoved.class);
        Mockito.when(removedItem.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(removedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(removedItem.getIneligibleStatus()).thenReturn(IneligibleStatus.INVENTORY_IS_DISCARDED);

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.empty());

        Mockito.when(shipmentItemRemovedRepository.findAllByShipmentId(1L)).thenReturn(Flux.just(removedItem));
        Mockito.when(shipmentItemPackedRepository.listAllIneligibleByShipmentId(1L)).thenReturn(Flux.just(tobeRemovedItem));

        Mono<RuleResponseDTO> removeDetail = useCase.removeItem(RemoveItemRequest.builder()
            .unitNumber("UN")
            .shipmentId(1L)
            .employeeId("test")
            .productCode("123")
            .build());

        StepVerifier
            .create(removeDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("The verification does not match all products in this order. Please re-scan all the products.", firstNotification.message());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("BAD_REQUEST", firstNotification.name());


                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var removeDetails = (RemoveProductResponseDTO) firstRuleResult;
                var firstRemovedItem = removeDetails.removedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstRemovedItem.unitNumber());
                assertEquals("PRODUCT_CODE", firstRemovedItem.productCode());
                assertEquals(IneligibleStatus.INVENTORY_IS_DISCARDED, firstRemovedItem.ineligibleStatus());

                var firstTobeRemovedItem = removeDetails.toBeRemovedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstTobeRemovedItem.unitNumber());
                assertEquals("ABCD", firstTobeRemovedItem.productCode());
                assertEquals(SecondVerification.COMPLETED, firstTobeRemovedItem.secondVerification());
                assertEquals("VERIFY_EMPLOYEE_ID", firstTobeRemovedItem.verifiedByEmployeeId());
                assertEquals(IneligibleStatus.INVENTORY_IS_EXPIRED.label, firstTobeRemovedItem.ineligibleStatus());
            })
            .verifyComplete();

    }

    @Test
    public void shouldRemoveShipmentItem() {

        var tobeRemovedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(tobeRemovedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(tobeRemovedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(tobeRemovedItem.getSecondVerification()).thenReturn(SecondVerification.COMPLETED);
        Mockito.when(tobeRemovedItem.getVerifiedByEmployeeId()).thenReturn("VERIFY_EMPLOYEE_ID");
        Mockito.when(tobeRemovedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");
        Mockito.when(tobeRemovedItem.getIneligibleStatus()).thenReturn(IneligibleStatus.INVENTORY_IS_EXPIRED);
        Mockito.when(tobeRemovedItem.getIneligibleAction()).thenReturn("TRIGGER_DISCARD");
        Mockito.when(tobeRemovedItem.getIneligibleReason()).thenReturn("EXPIRED");
        Mockito.when(tobeRemovedItem.getIneligibleMessage()).thenReturn("EXPIRED_MESSAGE");


        Mockito.when(shipmentItemPackedRepository.findByUnitTobeRemoved(1L,"UN","123")).thenReturn(Mono.just(tobeRemovedItem));


        var removedItem = Mockito.mock(ShipmentItemRemoved.class);
        Mockito.when(removedItem.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(removedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(removedItem.getIneligibleStatus()).thenReturn(IneligibleStatus.INVENTORY_IS_DISCARDED);

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.empty());

        Mockito.when(shipmentItemRemovedRepository.findAllByShipmentId(1L)).thenReturn(Flux.just(removedItem));
        Mockito.when(shipmentItemPackedRepository.listAllIneligibleByShipmentId(1L)).thenReturn(Flux.just(tobeRemovedItem));
        Mockito.when(shipmentItemPackedRepository.delete(Mockito.any())).thenReturn(Mono.empty());

        Mockito.when(shipmentItemRemovedRepository.save(Mockito.any())).thenReturn(Mono.just(removedItem));

        Mono<RuleResponseDTO> removeDetail = useCase.removeItem(RemoveItemRequest.builder()
            .unitNumber("UN")
            .shipmentId(1L)
            .employeeId("test")
            .productCode("123")
            .build());

        StepVerifier
            .create(removeDetail)
            .consumeNextWith(detail -> {

                assertEquals(HttpStatus.OK, detail.ruleCode());

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var removeDetails = (RemoveProductResponseDTO) firstRuleResult;
                var firstRemovedItem = removeDetails.removedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstRemovedItem.unitNumber());
                assertEquals("PRODUCT_CODE", firstRemovedItem.productCode());
                assertEquals(IneligibleStatus.INVENTORY_IS_DISCARDED, firstRemovedItem.ineligibleStatus());

                var firstTobeRemovedItem = removeDetails.toBeRemovedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstTobeRemovedItem.unitNumber());
                assertEquals("ABCD", firstTobeRemovedItem.productCode());
                assertEquals(SecondVerification.COMPLETED, firstTobeRemovedItem.secondVerification());
                assertEquals("VERIFY_EMPLOYEE_ID", firstTobeRemovedItem.verifiedByEmployeeId());
                assertEquals(IneligibleStatus.INVENTORY_IS_EXPIRED.label, firstTobeRemovedItem.ineligibleStatus());

                var responseRemovedItem = removeDetails.removedItem();
                assertNotNull(responseRemovedItem);
                assertEquals("UNIT_NUMBER", responseRemovedItem.unitNumber());
                assertEquals("ABCD", responseRemovedItem.productCode());
                assertEquals(IneligibleStatus.INVENTORY_IS_EXPIRED.label, responseRemovedItem.ineligibleStatus());
                assertEquals("TRIGGER_DISCARD", responseRemovedItem.ineligibleAction());
                assertEquals("EXPIRED", responseRemovedItem.ineligibleReason());
                assertEquals("EXPIRED_MESSAGE", responseRemovedItem.ineligibleMessage());


            })
            .verifyComplete();

    }
}
