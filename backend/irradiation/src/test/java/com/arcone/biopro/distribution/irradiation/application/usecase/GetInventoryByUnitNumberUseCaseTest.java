package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class GetInventoryByUnitNumberUseCaseTest {

    @InjectMocks
    private GetInventoryByUnitNumberUseCase getInventoryByUnitNumberUseCase;

    @Mock
    private InventoryAggregateRepository inventoryRepository;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @BeforeEach
    void setUp() {
        openMocks(this);
    }


    /**
     * Test case for execute method when a valid unit number is provided.
     * It verifies that the method correctly retrieves irradiation data from the repository,
     * maps it to the output format, and returns the expected Flux of InventoryOutput.
     */
    @Test
    public void testExecuteWithValidUnitNumber() {
        String unitNumber = "W036000000012";
        InventoryAggregate aggregate = InventoryAggregate.builder().inventory(Inventory.builder().unitNumber(new UnitNumber(unitNumber)).build()).build();

        when(inventoryRepository.findByUnitNumber(unitNumber)).thenReturn(Flux.just(aggregate));

        Flux<InventoryOutput> result = getInventoryByUnitNumberUseCase.execute(unitNumber);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.unitNumber().equals(unitNumber))
                .verifyComplete();

        Mockito.verify(inventoryRepository).findByUnitNumber(unitNumber);
    }

}
