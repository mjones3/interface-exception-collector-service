package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PageOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.PageOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentQueryCommandInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.ShipmentSearchUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Page;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class ShipmentSearchUseCaseTest {

    @Mock
    private RecoveredPlasmaShipmentQueryCommandInputMapper recoveredPlasmaShipmentQueryCommandInputMapper;

    @Mock
    private PageOutputMapper pageOutputMapper;

    @Mock
    private RecoveredPlasmaShipmentReportRepository recoveredPlasmaShipmentReportRepository;

    @InjectMocks
    private ShipmentSearchUseCase shipmentSearchUseCase;

    @Test
    void shouldReturnSuccessfulSearchResults() {
        // Arrange
        RecoveredPlasmaShipmentQueryCommandInput input = RecoveredPlasmaShipmentQueryCommandInput.builder().build();
        RecoveredPlasmaShipmentQueryCommand model =  new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), "SHIP001", List.of("OPEN"), List.of("CUST1"),
            List.of("TYPE1"), null, null,
            null, 1, 10, null
        );
        Page<RecoveredPlasmaShipmentReport> page = Mockito.mock(Page.class);
        PageOutput<RecoveredPlasmaShipmentReportOutput> pageOutput = Mockito.mock(PageOutput.class);

        Mockito.when(recoveredPlasmaShipmentQueryCommandInputMapper.toModel(input)).thenReturn(model);
        Mockito.when(recoveredPlasmaShipmentReportRepository.search(model)).thenReturn(Mono.just(page));

        var report = Mockito.mock(RecoveredPlasmaShipmentReport.class);
        Mockito.when(page.getContent()).thenReturn(List.of(report));
        Mockito.when(pageOutputMapper.toPageOutput(page)).thenReturn(pageOutput);

        // Act
        StepVerifier.create(shipmentSearchUseCase.search(input))
            .expectNextMatches(result ->
                result.notifications() == null &&
                    result.data() == pageOutput &&
                    result._links() == null
            )
            .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenNoResultsFound() {
        // Arrange
        RecoveredPlasmaShipmentQueryCommandInput input = RecoveredPlasmaShipmentQueryCommandInput.builder().build();
        RecoveredPlasmaShipmentQueryCommand model =  new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), "SHIP001", List.of("OPEN"), List.of("CUST1"),
            List.of("TYPE1"), null, null,
            null, 1, 10, null
        );

        Mockito.when(recoveredPlasmaShipmentQueryCommandInputMapper.toModel(input)).thenReturn(model);
        Mockito.when(recoveredPlasmaShipmentReportRepository.search(model)).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(shipmentSearchUseCase.search(input))
            .expectNextMatches(result ->
                result.notifications() != null &&
                    result.notifications().size() == 1 &&
                    result.notifications().get(0).useCaseMessage().getType() == UseCaseNotificationType.CAUTION &&
                    result.data() == null
            )
            .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenContentIsEmpty() {
        // Arrange

        RecoveredPlasmaShipmentQueryCommandInput input = RecoveredPlasmaShipmentQueryCommandInput.builder().build();
        RecoveredPlasmaShipmentQueryCommand model =  new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), "SHIP001", List.of("OPEN"), List.of("CUST1"),
            List.of("TYPE1"), null, null,
            null, 1, 10, null
        );

        Page<RecoveredPlasmaShipmentReport> page = Mockito.mock(Page.class);

        Mockito.when(recoveredPlasmaShipmentQueryCommandInputMapper.toModel(input)).thenReturn(model);
        Mockito.when(recoveredPlasmaShipmentReportRepository.search(model)).thenReturn(Mono.just(page));
        Mockito.when(page.getContent()).thenReturn(Collections.emptyList());

        // Act
        StepVerifier.create(shipmentSearchUseCase.search(input))
            .expectNextMatches(result ->
                result.notifications() != null &&
                    result.notifications().size() == 1 &&
                    result.notifications().get(0).useCaseMessage().getType() == UseCaseNotificationType.CAUTION &&
                    result.data() == null
            )
            .verifyComplete();
    }

    @Test
    void shouldHandleGeneralErrors() {
        // Arrange
        RecoveredPlasmaShipmentQueryCommandInput input = RecoveredPlasmaShipmentQueryCommandInput.builder().build();
        RecoveredPlasmaShipmentQueryCommand model =  new RecoveredPlasmaShipmentQueryCommand(
            List.of("LOC1"), "SHIP001", List.of("OPEN"), List.of("CUST1"),
            List.of("TYPE1"), null, null,
            null, 1, 10, null
        );

        RuntimeException testException = new RuntimeException("Test error");

        Mockito.when(recoveredPlasmaShipmentQueryCommandInputMapper.toModel(input)).thenReturn(model);
        Mockito.when(recoveredPlasmaShipmentReportRepository.search(model)).thenReturn(Mono.error(testException));

        // Act
        StepVerifier.create(shipmentSearchUseCase.search(input))
            .expectNextMatches(result ->
                result.notifications() != null &&
                    result.notifications().size() == 1 &&
                    result.notifications().get(0).useCaseMessage().getType() == UseCaseNotificationType.CAUTION &&
                    result.notifications().get(0).useCaseMessage().getMessage().equals("Test error") &&
                    result.data() == null
            )
            .verifyComplete();
    }
}

