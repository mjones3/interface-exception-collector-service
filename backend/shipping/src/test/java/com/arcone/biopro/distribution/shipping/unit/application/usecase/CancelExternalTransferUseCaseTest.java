package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.CancelExternalTransferRequest;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.CancelExternalTransferUseCase;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CancelExternalTransferUseCaseTest {


    private CancelExternalTransferUseCase useCase;
    private ExternalTransferRepository externalTransferRepository;
    private ExternalTransferDomainMapper externalTransferDomainMapper;

    @BeforeEach
    void setUp() {
        externalTransferRepository = Mockito.mock(ExternalTransferRepository.class);
        useCase = new CancelExternalTransferUseCase(externalTransferRepository);

    }

    @Test
    public void shouldNotCancelTransferWhenTransferDoesNotExist() {

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(useCase.cancelExternalTransfer(CancelExternalTransferRequest
                .builder()
                .employeeId("employeeId")
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
    public void shouldNotConfirmCancelTransferWhenTransferDoesNotExist() {

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(useCase.confirmCancelExternalTransfer(CancelExternalTransferRequest
                .builder()
                .employeeId("employeeId")
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
    public void shouldCancelTransfer() {

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(Mockito.mock(ExternalTransfer.class)));

        StepVerifier
            .create(useCase.cancelExternalTransfer(CancelExternalTransferRequest
                .builder()
                .employeeId("employeeId")
                .externalTransferId(1L)
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                Assertions.assertEquals(HttpStatus.OK, detail.ruleCode());
                Assertions.assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                Assertions.assertEquals("CONFIRMATION", firstNotification.notificationType());
                Assertions.assertEquals("When cancelling, all external transfer information will be removed. Are you sure you want to cancel?", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldConfirmCancelTransfer() {

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(Mockito.mock(ExternalTransfer.class)));
        Mockito.when(externalTransferRepository.deleteOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(useCase.confirmCancelExternalTransfer(CancelExternalTransferRequest
                .builder()
                .employeeId("employeeId")
                .externalTransferId(1L)
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                Assertions.assertEquals(HttpStatus.OK, detail.ruleCode());
                Assertions.assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                Assertions.assertEquals("SUCCESS", firstNotification.notificationType());
                Assertions.assertEquals("External transfer cancellation completed", firstNotification.message());
                Assertions.assertEquals("/external-transfer", detail._links().get("next"));
            })
            .verifyComplete();

    }



}
