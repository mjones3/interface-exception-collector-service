package com.arcone.biopro.distribution.inventory.application.usecase;


import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductReceivedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductsReceivedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ProductsReceivedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private ProductsReceivedUseCase productsReceivedUseCase;

    private ProductsReceivedInput productsReceivedInput;
    private InventoryAggregate inventoryAggregate;

    @BeforeEach
    void setUp() {
        openMocks(this);

        ProductReceivedInput productInput = ProductReceivedInput.builder()
            .unitNumber("W036589878681")
            .productCode("E6170V00")
            .inventoryLocation("123456789")
            .quarantines("FAILED_VISUAL_INSPECTION")
            .build();

        productsReceivedInput = ProductsReceivedInput.builder()
            .locationCode("123456789")
            .products(List.of(productInput))
            .build();

        Inventory inventory = Inventory.builder()
            .unitNumber(new UnitNumber("W036589878681"))
            .productCode(new ProductCode("E6170V00"))
            .inventoryStatus(InventoryStatus.IN_TRANSIT)
            .inventoryLocation("OLD_LOCATION")
            .build();
        inventoryAggregate = InventoryAggregate.builder().inventory(inventory).build();
    }

    @Test
    void execute_ShouldUpdateInventoryLocationAndStatus_WhenProductIsReceived() {
        // Arrange
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString()))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryAggregateRepository.saveInventory(any(InventoryAggregate.class)))
            .thenReturn(Mono.just(inventoryAggregate));

        // Act
        Mono<InventoryOutput> result = productsReceivedUseCase.execute(productsReceivedInput);

        // Assert
        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode("W036589878681", "E6170V00");
        verify(inventoryAggregateRepository).saveInventory(inventoryAggregate);
    }
}
