package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PrintUnacceptableUnitReportCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UnacceptableUnitReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.PrintUnacceptableUnitsReportUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.UnacceptableUnitReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PrintUnacceptableUnitsReportUseCaseTest {

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private SystemProcessPropertyRepository systemProcessPropertyRepository;
    @Mock
    private UnacceptableUnitReportRepository unacceptableUnitReportRepository;
    @Mock
    private RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;

    @InjectMocks
    private PrintUnacceptableUnitsReportUseCase useCase;

    @Test
    void shouldSuccessfullyPrintUnacceptableUnitReport() {
        // Given
        Long shipmentId = 123L;
        String employeeId = "EMP456";
        String locationCode = "LOC789";

        PrintUnacceptableUnitReportCommandInput commandInput = new PrintUnacceptableUnitReportCommandInput(
            shipmentId, employeeId, locationCode
        );

        RecoveredPlasmaShipment shipment = createMockShipment();
        UnacceptableUnitReport report = createMockUnacceptableUnitReport();
        UnacceptableUnitReportOutput reportOutput = createMockUnacceptableUnitReportOutput();

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.just(shipment));
        when(recoveredPlasmaShipmentOutputMapper.toUnacceptableUnitReportOutput(any()))
            .thenReturn(reportOutput);

        // Mock the domain method call
        mockPrintUnacceptableUnitReport(shipment, report);

        // When
        StepVerifier.create(useCase.printUnacceptableUnitReport(commandInput))
            // Then
            .expectNextMatches(output -> {
                assertThat(output.notifications()).hasSize(1);
                assertThat(output.notifications().get(0).useCaseMessage().type())
                    .isEqualTo(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_SUCCESS.getType());
                assertThat(output.data()).isEqualTo(reportOutput);
                return true;
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleShipmentNotFound() {
        // Given
        Long shipmentId = 1L;
        PrintUnacceptableUnitReportCommandInput commandInput = new PrintUnacceptableUnitReportCommandInput(
            shipmentId, "EMP456", "LOC789"
        );

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.empty());

        // When
        StepVerifier.create(useCase.printUnacceptableUnitReport(commandInput))
            // Then
            .expectNextMatches(output -> {
                assertThat(output.notifications()).hasSize(1);
                assertThat(output.notifications().get(0).useCaseMessage().type())
                    .isEqualTo(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_ERROR.getType());
                assertThat(output.data()).isNull();
                return true;
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleErrorDuringPrinting() {
        // Given
        Long shipmentId = 123L;
        PrintUnacceptableUnitReportCommandInput commandInput = new PrintUnacceptableUnitReportCommandInput(
            shipmentId, "EMP456", "LOC789"
        );

        RecoveredPlasmaShipment shipment = createMockShipment();

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.just(shipment));

        // Mock an error during printing
        when(shipment.printUnacceptableUnitReport(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Printing error"));

        // When
        StepVerifier.create(useCase.printUnacceptableUnitReport(commandInput))
            // Then
            .expectNextMatches(output -> {
                assertThat(output.notifications()).hasSize(1);
                assertThat(output.notifications().get(0).useCaseMessage().type())
                    .isEqualTo(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_ERROR.getType());
                assertThat(output.data()).isNull();
                return true;
            })
            .verifyComplete();
    }

    private RecoveredPlasmaShipment createMockShipment() {
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        return shipment;
    }

    private UnacceptableUnitReport createMockUnacceptableUnitReport() {
        return Mockito.mock(UnacceptableUnitReport.class);
    }

    private UnacceptableUnitReportOutput createMockUnacceptableUnitReportOutput() {
        return UnacceptableUnitReportOutput.
            builder()
            .reportTitle("TEST")
            .shipmentNumber("SHIP123")
            .build();
    }

    private void mockPrintUnacceptableUnitReport(RecoveredPlasmaShipment shipment, UnacceptableUnitReport report) {
        when(shipment.printUnacceptableUnitReport(any(), any(), any(), any()))
            .thenReturn(report);
    }
}
