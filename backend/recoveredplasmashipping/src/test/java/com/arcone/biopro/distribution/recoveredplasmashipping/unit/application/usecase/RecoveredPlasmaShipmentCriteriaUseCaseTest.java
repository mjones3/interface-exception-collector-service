package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ProductTypeOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentCriteriaOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.RecoveredPlasmaShipmentCriteriaUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;


@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentCriteriaUseCaseTest {

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;

    @Mock
    private RecoveredPlasmaShipmentCriteriaOutputMapper recoveredPlasmaShipmentCriteriaMapper;

    @InjectMocks
    private RecoveredPlasmaShipmentCriteriaUseCase recoveredPlasmaShipmentCriteriaUseCase;

    @Test
    void findAllProductTypeByCustomer_WhenProductsExist_ShouldReturnMappedProducts() {
        // Given
        String customerCode = "CUSTOMER123";
        ProductType productType = new ProductType(1,"TYPE-1","TYPE-1"); // Assume this is your domain entity
        ProductTypeOutput productTypeOutput = ProductTypeOutput.builder()
            .id(1)
            .productType("TYPE-1")
            .productTypeDescription("TYPE-1")
            .build(); // Assume this is your output DTO

        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findAllProductTypeByByCustomerCode(customerCode))
            .thenReturn(Flux.just(productType));
        Mockito.when(recoveredPlasmaShipmentCriteriaMapper.toOutput(productType))
            .thenReturn(productTypeOutput);

        // When
        Flux<ProductTypeOutput> result = recoveredPlasmaShipmentCriteriaUseCase
            .findAllProductTypeByCustomer(customerCode);

        // Then
        StepVerifier.create(result)
            .expectNext(productTypeOutput)
            .verifyComplete();

        Mockito.verify(recoveredPlasmaShipmentCriteriaRepository).findAllProductTypeByByCustomerCode(customerCode);
        Mockito.verify(recoveredPlasmaShipmentCriteriaMapper).toOutput(productType);
    }

    @Test
    void findAllProductTypeByCustomer_WhenNoProducts_ShouldThrowNoResultsFoundException() {
        // Given
        String customerCode = "CUSTOMER123";
        Mockito.when(recoveredPlasmaShipmentCriteriaRepository.findAllProductTypeByByCustomerCode(customerCode))
            .thenReturn(Flux.empty());

        // When
        Flux<ProductTypeOutput> result = recoveredPlasmaShipmentCriteriaUseCase
            .findAllProductTypeByCustomer(customerCode);

        // Then
        StepVerifier.create(result)
            .expectError(NoResultsFoundException.class)
            .verify();

        Mockito.verify(recoveredPlasmaShipmentCriteriaRepository).findAllProductTypeByByCustomerCode(customerCode);
        Mockito.verify(recoveredPlasmaShipmentCriteriaMapper, Mockito.never()).toOutput(Mockito.any(ProductType.class));
    }
}

