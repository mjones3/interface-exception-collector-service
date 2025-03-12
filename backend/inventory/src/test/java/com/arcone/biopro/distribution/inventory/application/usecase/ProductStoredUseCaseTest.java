package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductStorageInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.common.TestUtil;
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
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductStoredUseCaseTest {

    private static final String UNIT_NUMBER = "W123456789012";
    private static final String PRODUCT_CODE = "E0980V99";
    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    static String LOCATION_1 = "LOCATION_1";
    static String STORAGE_LOCATION = "Bin001,Shelf002,Tray001";
    static String DEVICE_STORED = "Freezer001";

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @InjectMocks
    private ProductStoredUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_should_updated_stored_information() {
        ProductStorageInput input = createInput();

        when(inventoryAggregateRepository.findByLocationAndUnitNumberAndProductCode(any(), any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));

        when(inventoryAggregateRepository.saveInventory(any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.DISCARDED, LocalDateTime.now().plusDays(1))));

        Mono<InventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.inventoryStatus()).isEqualTo(InventoryStatus.DISCARDED);
            })
            .verifyComplete();

        ArgumentCaptor<InventoryAggregate> laCaptor = ArgumentCaptor.forClass(InventoryAggregate.class);
        verify(inventoryAggregateRepository).saveInventory(laCaptor.capture());
        InventoryAggregate inventoryAggregateUpdated = laCaptor.getValue();

        assertEquals(InventoryStatus.AVAILABLE, inventoryAggregateUpdated.getInventory().getInventoryStatus());
        assertEquals(STORAGE_LOCATION, inventoryAggregateUpdated.getInventory().getStorageLocation());
        assertEquals(DEVICE_STORED, inventoryAggregateUpdated.getInventory().getDeviceStored());
    }

    private static ProductStorageInput createInput() {
        return ProductStorageInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .location(LOCATION_1)
            .deviceStored(DEVICE_STORED)
            .storageLocation(STORAGE_LOCATION)
            .build();
    }

    @Test
    void execute_shouldValidate_inventory_is_not_found() {
        ProductStorageInput input = createInput();

        when(inventoryAggregateRepository.findByLocationAndUnitNumberAndProductCode(any(), any(), any()))
            .thenReturn(Mono.empty());

        Mono<InventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .expectError(InventoryNotFoundException.class)
            .verify();
    }

    private InventoryAggregate createInventoryAggregate(InventoryStatus status, LocalDateTime expirationDate) {
        return InventoryAggregate.builder()
                .inventory(Inventory.builder()
                    .id(UUID.randomUUID())
                    .unitNumber(new UnitNumber(UNIT_NUMBER))
                    .productCode(new ProductCode(PRODUCT_CODE))
                    .location(LOCATION_1)
                    .inventoryStatus(status)
                    .expirationDate(expirationDate)
                    .quarantines(TestUtil.createQuarantines())
                    .build())
            .build();
    }




}
