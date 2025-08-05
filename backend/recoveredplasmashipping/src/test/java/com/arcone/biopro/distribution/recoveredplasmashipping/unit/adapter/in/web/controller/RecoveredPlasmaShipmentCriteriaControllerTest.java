package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller.RecoveredPlasmaShipmentCriteriaController;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.ProductTypeDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.RecoveredPlasmaShipmentCriteriaDtoMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ProductTypeOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentCriteriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentCriteriaControllerTest {

    @Mock
    private RecoveredPlasmaShipmentCriteriaService recoveredPlasmaShipmentCriteriaService;

    @Mock
    private RecoveredPlasmaShipmentCriteriaDtoMapper recoveredPlasmaShipmentCriteriaDtoMapper;

    @InjectMocks
    private RecoveredPlasmaShipmentCriteriaController recoveredPlasmaShipmentCriteriaController;

    @Test
    void findAllProductTypeByCustomer_WhenProductsExist_ShouldReturnMappedDTOs() {
        // Given
        String customerCode = "CUSTOMER123";
        ProductTypeOutput productTypeOutput = new ProductTypeOutput(1,"TYPE","DESCRIPTION"); // Your domain object
        ProductTypeDTO productTypeDTO = new ProductTypeDTO(1,"TYPE","DESCRIPTION"); // Your DTO object

        when(recoveredPlasmaShipmentCriteriaService.findAllProductTypeByCustomer(customerCode))
            .thenReturn(Flux.just(productTypeOutput));
        when(recoveredPlasmaShipmentCriteriaDtoMapper.toDto(productTypeOutput))
            .thenReturn(productTypeDTO);

        // When
        Flux<ProductTypeDTO> result = recoveredPlasmaShipmentCriteriaController
            .findAllProductTypeByCustomer(customerCode);

        // Then
        StepVerifier.create(result)
            .expectNext(productTypeDTO)
            .verifyComplete();

        verify(recoveredPlasmaShipmentCriteriaService).findAllProductTypeByCustomer(customerCode);
        verify(recoveredPlasmaShipmentCriteriaDtoMapper).toDto(productTypeOutput);
    }

    @Test
    void findAllProductTypeByCustomer_WhenNoProducts_ShouldPropagateEmpty() {
        // Given
        String customerCode = "CUSTOMER123";
        when(recoveredPlasmaShipmentCriteriaService.findAllProductTypeByCustomer(customerCode))
            .thenReturn(Flux.empty());

        // When
        Flux<ProductTypeDTO> result = recoveredPlasmaShipmentCriteriaController
            .findAllProductTypeByCustomer(customerCode);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(recoveredPlasmaShipmentCriteriaService).findAllProductTypeByCustomer(customerCode);
        verify(recoveredPlasmaShipmentCriteriaDtoMapper, never()).toDto(any(ProductTypeOutput.class));
    }

    @Test
    void findAllProductTypeByCustomer_WhenServiceThrowsException_ShouldPropagateError() {
        // Given
        String customerCode = "CUSTOMER123";
        NoResultsFoundException exception = new NoResultsFoundException();

        when(recoveredPlasmaShipmentCriteriaService.findAllProductTypeByCustomer(customerCode))
            .thenReturn(Flux.error(exception));

        // When
        Flux<ProductTypeDTO> result = recoveredPlasmaShipmentCriteriaController
            .findAllProductTypeByCustomer(customerCode);

        // Then
        StepVerifier.create(result)
            .expectError(NoResultsFoundException.class)
            .verify();

        verify(recoveredPlasmaShipmentCriteriaService).findAllProductTypeByCustomer(customerCode);
        verify(recoveredPlasmaShipmentCriteriaDtoMapper, never()).toDto(any(ProductTypeOutput.class));
    }
}

