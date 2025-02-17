package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.ExternalTransferDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.CreateExternalTransferUseCase;
import com.arcone.biopro.distribution.shipping.domain.model.CreateExternalTransferCommand;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

class CreateExternalTransferUseCaseTest {

    private CreateExternalTransferUseCase createExternalTransferUseCase;
    private ExternalTransferRepository  externalTransferRepository;
    private ExternalTransferDomainMapper externalTransferDomainMapper;

    @BeforeEach
    void setUp() {
        externalTransferRepository = Mockito.mock(ExternalTransferRepository.class);
        externalTransferDomainMapper = Mockito.mock(ExternalTransferDomainMapper.class);
        createExternalTransferUseCase = new CreateExternalTransferUseCase(externalTransferRepository, externalTransferDomainMapper);

    }

    @Test
    public void shouldNotCreateExternalTransferWhenError(){

        Mockito.when(externalTransferDomainMapper.toDomain(Mockito.any())).thenReturn(Mono.just(Mockito.mock(ExternalTransfer.class)));

        Mockito.when(externalTransferRepository.create(Mockito.any())).thenReturn(Mono.error(new RuntimeException("Not able to create")));

        StepVerifier
            .create(createExternalTransferUseCase.createExternalTransfer(new CreateExternalTransferCommand("123"
                ,"123", LocalDate.now(),"employee-id")))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("Not able to create", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotCreateExternalTransfer(){

        Mockito.when(externalTransferDomainMapper.toDomain(Mockito.any())).thenReturn(Mono.just(Mockito.mock(ExternalTransfer.class)));
        Mockito.when(externalTransferDomainMapper.toDTO(Mockito.any())).thenReturn(ExternalTransferDTO.builder().build());

        var externalTransfer = Mockito.mock(ExternalTransfer.class);

        Mockito.when(externalTransferRepository.create(Mockito.any())).thenReturn(Mono.just(externalTransfer));

        StepVerifier
            .create(createExternalTransferUseCase.createExternalTransfer(new CreateExternalTransferCommand("123"
                ,"123", LocalDate.now(),"employee-id")))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                assertEquals("SUCCESS", firstNotification.notificationType());
                assertEquals("External Transfer created", firstNotification.message());
            })
            .verifyComplete();

    }

}
