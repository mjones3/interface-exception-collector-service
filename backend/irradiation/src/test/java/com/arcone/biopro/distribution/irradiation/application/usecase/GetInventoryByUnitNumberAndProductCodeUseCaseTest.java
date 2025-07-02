package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.GetInventoryByUnitNumberAndProductInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

public class GetInventoryByUnitNumberAndProductCodeUseCaseTest {

    /**
     * Test case for execute method when a valid input is provided.
     * It verifies that the method correctly retrieves irradiation data,
     * maps it to the output, and returns the expected InventoryOutput.
     */
    @Test
    public void test_execute_validInput_returnsInventoryOutput() {
        // Arrange
        InventoryAggregateRepository repository = mock(InventoryAggregateRepository.class);
        InventoryOutputMapper mapper = mock(InventoryOutputMapper.class);
        GetInventoryByUnitNumberAndProductCodeUseCase useCase = new GetInventoryByUnitNumberAndProductCodeUseCase(repository, mapper);

        String unitNumber = "W036800000012";
        String productCode = "E04400";
        GetInventoryByUnitNumberAndProductInput input = new GetInventoryByUnitNumberAndProductInput(unitNumber, productCode);

        InventoryAggregate aggregate = mock(InventoryAggregate.class);
        Inventory inventory = mock(Inventory.class);
        InventoryOutput expectedOutput = mock(InventoryOutput.class);

        when(repository.findByUnitNumberAndProductCode(unitNumber, productCode)).thenReturn(Mono.just(aggregate));
        when(aggregate.getInventory()).thenReturn(inventory);
        when(aggregate.isExpired()).thenReturn(false);

        when(mapper.toOutput(any(), anyBoolean())).thenReturn(expectedOutput);

        // Act
        Mono<InventoryOutput> result = useCase.execute(input);

        // Assert
        StepVerifier.create(result)
            .expectNext(expectedOutput)
            .verifyComplete();

        verify(repository).findByUnitNumberAndProductCode(unitNumber, productCode);
        verify(mapper).toOutput(inventory, Boolean.FALSE);
    }

}
