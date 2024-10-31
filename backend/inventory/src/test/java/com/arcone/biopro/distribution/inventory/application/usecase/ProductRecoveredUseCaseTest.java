package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductRecoveredInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.inventory.domain.service.TextConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ProductRecoveredUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Mock
    TextConfigService textConfigService;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private ProductRecoveredUseCase productRecoveredUseCase;

    private ProductRecoveredInput productRecoveredInput;
    private InventoryAggregate inventoryAggregate;

    @BeforeEach
    void setUp() {
        openMocks(this);

        productRecoveredInput = new ProductRecoveredInput("W036824111111","E1624V00");

        Inventory inventory = Inventory.builder()
            .unitNumber(new UnitNumber("W036824111111"))
            .productCode(new ProductCode("E1624V00"))
            .inventoryStatus(InventoryStatus.DISCARDED)
            .statusReason("EXPIRED")
            .comments("Some comments")
            .histories(new ArrayList<>(List.of(new History(InventoryStatus.AVAILABLE, null, null))))
            .build();

        inventoryAggregate = InventoryAggregate.builder().inventory(inventory).build();

        mapper.setTextConfigService(textConfigService);

        when(textConfigService.getText(anyString(), anyString())).thenReturn("");
    }

    @Test
    void execute_ShouldReturnInventoryOutput_WhenInventoryIsFoundAndRecovered() {
        // Arrange
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString()))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryAggregateRepository.saveInventory(any(InventoryAggregate.class)))
            .thenReturn(Mono.just(inventoryAggregate));

        // Act
        Mono<InventoryOutput> result = productRecoveredUseCase.execute(productRecoveredInput);

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output.inventoryStatus()).isEqualTo(InventoryStatus.AVAILABLE);
            })
            .verifyComplete();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode("W036824111111", "E1624V00");
        verify(inventoryAggregateRepository).saveInventory(inventoryAggregate);
    }

    @Test
    void execute_ShouldThrowInventoryNotFoundException_WhenInventoryIsNotFound() {
        // Arrange
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString()))
            .thenReturn(Mono.empty());

        // Act
        Mono<InventoryOutput> result = productRecoveredUseCase.execute(productRecoveredInput);

        // Assert
        StepVerifier.create(result)
            .expectError(InventoryNotFoundException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode("W036824111111", "E1624V00");
        verify(inventoryAggregateRepository, never()).saveInventory(any());
        verify(mapper, never()).toOutput(any(Inventory.class));
    }
}
