package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.CompleteExternalTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ExternalTransferDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.CompleteExternalTransferUseCase;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CompleteExternalTransferUseCaseTest {

    private CompleteExternalTransferUseCase completeExternalTransferUseCase;
    private ExternalTransferRepository externalTransferRepository;
    private ExternalTransferDomainMapper externalTransferDomainMapper;

    @BeforeEach
    void setUp() {
        externalTransferRepository = Mockito.mock(ExternalTransferRepository.class);
        externalTransferDomainMapper = Mockito.mock(ExternalTransferDomainMapper.class);
        completeExternalTransferUseCase = new CompleteExternalTransferUseCase(externalTransferRepository, externalTransferDomainMapper);

    }

    @Test
    public void shouldNotCompleteTransferWhenTransferDoesNotExist() {

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(completeExternalTransferUseCase.completeExternalTransfer(CompleteExternalTransferCommandDTO
                .builder()
                .employeeId("employeeId")
                    .hospitalTransferId("transfer-id")
                .externalTransferId(1L)
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                Assertions.assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                Assertions.assertEquals("WARN", firstNotification.notificationType());
                Assertions.assertEquals("External transfer not found", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotCompleteTransferWhenTransferWhenValidationFails() {

        var externalTransfer = Mockito.mock(ExternalTransfer.class);

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(externalTransfer));

        Mockito.doThrow(new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_CANNOT_BE_COMPLETED)).when(externalTransfer).complete(Mockito.anyString(),Mockito.anyString());


        StepVerifier
            .create(completeExternalTransferUseCase.completeExternalTransfer(CompleteExternalTransferCommandDTO
                .builder()
                .employeeId("employeeId")
                .hospitalTransferId("transfer-id")
                .externalTransferId(1L)
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                Assertions.assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                Assertions.assertEquals("WARN", firstNotification.notificationType());
                Assertions.assertEquals("External Transfer product list should have at least one product", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldCompleteTransfer(){

        var externalTransfer = Mockito.mock(ExternalTransfer.class);

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(externalTransfer));

        Mockito.when(externalTransferRepository.update(Mockito.any())).thenReturn(Mono.just(externalTransfer));

        Mockito.when(externalTransferDomainMapper.toDTO(Mockito.any())).thenReturn(Mockito.mock(ExternalTransferDTO.class));

        StepVerifier
            .create(completeExternalTransferUseCase.completeExternalTransfer(CompleteExternalTransferCommandDTO
                .builder()
                .employeeId("employeeId")
                .hospitalTransferId("transfer-id")
                .externalTransferId(1L)
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                Assertions.assertEquals(HttpStatus.OK, detail.ruleCode());
                Assertions.assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                Assertions.assertEquals("SUCCESS", firstNotification.notificationType());
                Assertions.assertEquals("External transfer completed successfully.", firstNotification.message());
                Assertions.assertEquals("/external-transfer", detail._links().get("next"));

            })
            .verifyComplete();

    }

}
