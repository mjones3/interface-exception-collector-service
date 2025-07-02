package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryInput;
import com.arcone.biopro.distribution.irradiation.application.dto.ValidateInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.common.TestUtil;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.Property;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.PropertyKey;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.irradiation.domain.service.TextConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ValidateInventoryUseCaseTest {

    private static final String UNIT_NUMBER = "W123456789012";
    private static final String PRODUCT_CODE = "E0980V99";
    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    String LOCATION_1 = "LOCATION_1";


    @Mock
    TextConfigService textConfigService;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private ValidateInventoryUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(textConfigService.getText(anyString(), anyString())).thenReturn("");
        mapper.setTextConfigService(textConfigService);
    }

    @Test
    void execute_shouldValidate_inventory_is_valid() {
        InventoryInput input = InventoryInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .inventoryLocation(LOCATION_1)
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));


        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notificationMessages().isEmpty()).isTrue();
            })
            .verifyComplete();
    }

    @Test
    void execute_shouldValidate_inventory_is_expired() {
        InventoryInput input = InventoryInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .inventoryLocation(LOCATION_1)
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().minusDays(1))));


        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notificationMessages().isEmpty()).isFalse();
                assertThat(output.notificationMessages().getFirst().name()).isEqualTo(MessageType.INVENTORY_IS_EXPIRED.name());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should Validate If Inventory Is In Quarantine")
    void execute_shouldValidate_inventory_is_quarantined() {
        InventoryInput input = InventoryInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .inventoryLocation(LOCATION_1)
            .build();

        var inventoryAggregate = createInventoryAggregate(InventoryStatus.SHIPPED, LocalDateTime.now().plusDays(1));
        inventoryAggregate.getInventory().setIsLabeled(Boolean.FALSE);
        inventoryAggregate.getInventory().setQuarantines(TestUtil.createQuarantines());
        inventoryAggregate.populateProperties(List.of(Property.builder().key(PropertyKey.QUARANTINED.name()).value("Y").build()));

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.just(inventoryAggregate));

        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notificationMessages().size()).isEqualTo(1);
                assertThat(output.notificationMessages().getFirst().name()).isEqualTo(MessageType.INVENTORY_IS_QUARANTINED.name());
            })
            .verifyComplete();
    }

    @Test
    void execute_shouldValidate_inventory_is_not_found() {
        InventoryInput input = new InventoryInput(UNIT_NUMBER, PRODUCT_CODE, null, null, true, 300, null, LOCATION_1, LOCATION_1, null, null, null);

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.empty());


        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notificationMessages().getFirst().name()).isEqualTo(MessageType.INVENTORY_NOT_EXIST.name());
            })
            .verifyComplete();
    }

    private InventoryAggregate createInventoryAggregate(InventoryStatus status, LocalDateTime expirationDate) {
        return InventoryAggregate.builder()
                .inventory(Inventory.builder()
                    .id(UUID.randomUUID())
                    .unitNumber(new UnitNumber(UNIT_NUMBER))
                    .productCode(new ProductCode(PRODUCT_CODE))
                    .inventoryLocation(LOCATION_1)
                    .inventoryStatus(status)
                    .expirationDate(expirationDate)
                    .isLabeled(Boolean.TRUE)
                    .build())
            .build();
    }




}
