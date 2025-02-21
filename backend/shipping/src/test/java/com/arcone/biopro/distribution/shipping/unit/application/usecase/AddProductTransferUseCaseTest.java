package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.AddProductTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ExternalTransferDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.AddProductTransferUseCase;
import com.arcone.biopro.distribution.shipping.domain.model.CreateExternalTransferCommand;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ProductLocationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.org.yaml.snakeyaml.constructor.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

class AddProductTransferUseCaseTest {


    private AddProductTransferUseCase addProductTransferUseCase;
    private ExternalTransferRepository externalTransferRepository;
    private ProductLocationHistoryRepository productLocationHistoryRepository;
    private ExternalTransferDomainMapper externalTransferDomainMapper;


    @BeforeEach
    void setUp() {
        externalTransferRepository = Mockito.mock(ExternalTransferRepository.class);
        productLocationHistoryRepository = Mockito.mock(ProductLocationHistoryRepository.class);
        externalTransferDomainMapper = Mockito.mock(ExternalTransferDomainMapper.class);
        addProductTransferUseCase = new AddProductTransferUseCase(externalTransferRepository,productLocationHistoryRepository,externalTransferDomainMapper);
    }

    @Test
    public void shouldAddProductTransfer() {

        var externalTransfer = Mockito.mock(ExternalTransfer.class);
        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(externalTransfer));

        Mockito.when(externalTransferRepository.update(Mockito.any())).thenReturn(Mono.just(externalTransfer));

        Mockito.when(externalTransferDomainMapper.toDTO(Mockito.any())).thenReturn(Mockito.mock(ExternalTransferDTO.class));


        StepVerifier
            .create(addProductTransferUseCase.addProductTransfer(AddProductTransferCommandDTO
                .builder()
                    .employeeId("employeeId")
                    .unitNumber("unitNumber")
                    .externalTransferId(1L)
                    .productCode("productCode")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                assertEquals("SUCCESS", firstNotification.notificationType());
                assertEquals("Product added successfully", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotAddProductTransferWhenTransferDoesNotExist() {

        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(addProductTransferUseCase.addProductTransfer(AddProductTransferCommandDTO
                .builder()
                .employeeId("employeeId")
                .unitNumber("unitNumber")
                .externalTransferId(1L)
                .productCode("productCode")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("External transfer not found", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotAddProductTransferWhenDomainValidationFails() {

        var externalTransfer = Mockito.mock(ExternalTransfer.class);
        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(externalTransfer));

        Mockito.doThrow(new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_DATE_BEFORE_SHIP_DATE)).when(externalTransfer).addItem(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

        StepVerifier
            .create(addProductTransferUseCase.addProductTransfer(AddProductTransferCommandDTO
                .builder()
                .employeeId("employeeId")
                .unitNumber("unitNumber")
                .externalTransferId(1L)
                .productCode("productCode")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("CAUTION", firstNotification.notificationType());
                assertEquals("The transfer date is before the last shipped date", firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotAddProductTransferWhenDuplicateRecords() {

        var externalTransfer = Mockito.mock(ExternalTransfer.class);
        Mockito.when(externalTransferRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(externalTransfer));

        Mockito.doThrow(new org.springframework.dao.DuplicateKeyException("Error database constraint")).when(externalTransfer).addItem(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

        StepVerifier
            .create(addProductTransferUseCase.addProductTransfer(AddProductTransferCommandDTO
                .builder()
                .employeeId("employeeId")
                .unitNumber("unitNumber")
                .externalTransferId(1L)
                .productCode("productCode")
                .build()))
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals("Product already added", firstNotification.message());
            })
            .verifyComplete();

    }

}
