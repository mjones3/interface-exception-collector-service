package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.VerifyProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.VerifyItemRequest;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.SecondVerificationUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
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
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
class SecondVerificationUseCaseTest {

    private ShipmentRepository shipmentRepository;

    private ShipmentItemPackedRepository shipmentItemPackedRepository;

    private ShipmentMapper shipmentMapper;
    private SecondVerificationUseCase useCase;

    @BeforeEach
    public void setUp() {
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        shipmentItemPackedRepository = Mockito.mock(ShipmentItemPackedRepository.class);
        shipmentMapper = new ShipmentMapper();
        useCase = new SecondVerificationUseCase(shipmentItemPackedRepository, shipmentRepository, shipmentMapper);
    }

    @Test
    public void shouldVerifyItem(){


        var verifiedItem = Mockito.mock(ShipmentItemPacked.class);

        Mockito.when(verifiedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(verifiedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(verifiedItem.getSecondVerification()).thenReturn(SecondVerification.PENDING);
        Mockito.when(verifiedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");

        var completePackedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(completePackedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(completePackedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(completePackedItem.getSecondVerification()).thenReturn(SecondVerification.COMPLETED);
        Mockito.when(completePackedItem.getVerifiedByEmployeeId()).thenReturn("VERIFY_EMPLOYEE_ID");
        Mockito.when(completePackedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");


        Mockito.when(shipmentItemPackedRepository.findByShipmentIUnitNumberAndProductCode(Mockito.anyLong() , Mockito.anyString() , Mockito.anyString())).thenReturn(Mono.just(verifiedItem));

        Mockito.when(shipmentItemPackedRepository.save(Mockito.any())).thenReturn(Mono.just(completePackedItem));

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(completePackedItem));

        Mockito.when(shipmentItemPackedRepository.listAllVerifiedByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(completePackedItem));

        Mono<RuleResponseDTO> response = useCase.verifyItem(VerifyItemRequest.builder()
                .shipmentId(1L)
            .unitNumber("UN")
            .employeeId("test")
            .productCode("123")
            .build());

        StepVerifier
            .create(response)
            .consumeNextWith(detail -> {
                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertNull(detail.notifications());
                assertNotNull(detail.results());

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var verificationDetails = (VerifyProductResponseDTO) firstRuleResult;
                var firstPackedItem = verificationDetails.packedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstPackedItem.unitNumber());
                assertEquals("ABCD", firstPackedItem.productCode());
                assertEquals("PACK_EMPLOYEE_ID", firstPackedItem.packedByEmployeeId());

                var firstVerifiedItem = verificationDetails.verifiedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstVerifiedItem.unitNumber());
                assertEquals("ABCD", firstVerifiedItem.productCode());
                assertEquals(SecondVerification.COMPLETED, firstVerifiedItem.secondVerification());
                assertEquals("VERIFY_EMPLOYEE_ID", firstVerifiedItem.verifiedByEmployeeId());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotVerifyItem(){

        Mockito.when(shipmentItemPackedRepository.findByShipmentIUnitNumberAndProductCode(Mockito.anyLong() , Mockito.anyString() , Mockito.anyString())).thenReturn(Mono.empty());

        var packedItem = Mockito.mock(ShipmentItemPacked.class);

        Mockito.when(packedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(packedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(packedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");


        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));

        Mockito.when(shipmentItemPackedRepository.save(Mockito.any())).thenReturn(Mono.just(packedItem));

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));

        Mockito.when(shipmentItemPackedRepository.listAllVerifiedByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));


        Mono<RuleResponseDTO> response = useCase.verifyItem(VerifyItemRequest.builder()
            .shipmentId(1L)
            .unitNumber("UN")
            .employeeId("test")
            .productCode("123")
            .build());


        StepVerifier
            .create(response)
            .consumeNextWith(detail -> {

                assertNotNull(detail.results());

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var verificationDetails = (VerifyProductResponseDTO) firstRuleResult;

                var firstPackedItem = verificationDetails.packedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstPackedItem.unitNumber());
                assertEquals("ABCD", firstPackedItem.productCode());
                assertEquals("PACK_EMPLOYEE_ID", firstPackedItem.packedByEmployeeId());

                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_UNIT_NOT_PACKED_ERROR, firstNotification.message());
            })
            .verifyComplete();


    }

    @Test
    public void shouldNotVerifyItemWhenIsAlreadyVerified(){

        var packedItem = Mockito.mock(ShipmentItemPacked.class);

        Mockito.when(packedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(packedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(packedItem.getSecondVerification()).thenReturn(SecondVerification.COMPLETED);
        Mockito.when(packedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");


        Mockito.when(shipmentItemPackedRepository.findByShipmentIUnitNumberAndProductCode(Mockito.anyLong() , Mockito.anyString() , Mockito.anyString())).thenReturn(Mono.just(packedItem));

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));

        Mockito.when(shipmentItemPackedRepository.save(Mockito.any())).thenReturn(Mono.just(packedItem));

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));

        Mockito.when(shipmentItemPackedRepository.listAllVerifiedByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(packedItem));


        Mono<RuleResponseDTO> response = useCase.verifyItem(VerifyItemRequest.builder()
            .shipmentId(1L)
            .unitNumber("UN")
            .employeeId("test")
            .productCode("123")
            .build());


        StepVerifier
            .create(response)
            .consumeNextWith(detail -> {

                assertNotNull(detail.results());

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var verificationDetails = (VerifyProductResponseDTO) firstRuleResult;

                var firstPackedItem = verificationDetails.packedItems().getFirst();
                assertEquals("UNIT_NUMBER", firstPackedItem.unitNumber());
                assertEquals("ABCD", firstPackedItem.productCode());
                assertEquals("PACK_EMPLOYEE_ID", firstPackedItem.packedByEmployeeId());

                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_ALREADY_COMPLETED_ERROR, firstNotification.message());
            })
            .verifyComplete();


    }

}
