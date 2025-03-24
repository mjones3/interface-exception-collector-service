package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GetAllAvailableInventoriesUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    String LOCATION_1 = "LOCATION_1";


    @Mock
    private InventoryOutputMapper mapper;

    @InjectMocks
    private GetAllAvailableInventoriesUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldReturnCorrectOutput_whenValidInputProvided() {
        // Given
        InventoryCriteria criteria = new InventoryCriteria("PLASMA_TRANSFUSABLE", AboRhCriteria.A, true, false, null);
        GetAllAvailableInventoriesInput input = new GetAllAvailableInventoriesInput(LOCATION_1, List.of(criteria));
        List<InventoryAggregate> aggregates = Collections.singletonList(InventoryAggregate.builder().build());
        Product product = new Product("W123456789012", "E0980V99", LOCATION_1, AboRhType.ABN);
        InventoryFamily inventoryFamily = new InventoryFamily("PLASMA_TRANSFUSABLE", AboRhCriteria.A, 1L, List.of(product));
        GetAllAvailableInventoriesOutput expectedOutput = new GetAllAvailableInventoriesOutput(LOCATION_1, List.of(inventoryFamily));

        when(inventoryAggregateRepository.findAllAvailableShortDate(any(), any(), any()))
            .thenReturn(Flux.fromIterable(aggregates));
        when(inventoryAggregateRepository.countAllAvailable(any(), any(), any()))
            .thenReturn(Mono.just(1L));
        when(mapper.toOutput(any(), any(), any(), any()))
            .thenReturn(inventoryFamily);
        when(mapper.toOutput(any(), any())).thenReturn(expectedOutput);

        Mono<GetAllAvailableInventoriesOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .expectNext(expectedOutput)
            .verifyComplete();
    }

    @Test
    void execute_shouldReturnEmptyOutput_whenNoCriteriaProvided() {

        String location = "TestLocation";
        GetAllAvailableInventoriesInput input = new GetAllAvailableInventoriesInput(location, Collections.emptyList());
        GetAllAvailableInventoriesOutput expectedOutput = new GetAllAvailableInventoriesOutput(location, Collections.emptyList());

        when(mapper.toOutput(any(), any())).thenReturn(expectedOutput);

        Mono<GetAllAvailableInventoriesOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .expectNext(expectedOutput)
            .verifyComplete();
    }
}
