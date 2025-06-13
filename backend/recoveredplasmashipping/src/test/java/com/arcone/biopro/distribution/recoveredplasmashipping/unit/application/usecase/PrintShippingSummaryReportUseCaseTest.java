package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PrintShippingSummaryReportCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.ShippingSummaryReportOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.PrintShippingSummaryReportUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PrintShippingSummaryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShippingSummaryReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrintShippingSummaryReportUseCaseTest {

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SystemProcessPropertyRepository systemProcessPropertyRepository;

    @Mock
    private ShippingSummaryReportOutputMapper shippingSummaryReportOutputMapper;

    @InjectMocks
    private PrintShippingSummaryReportUseCase useCase;

    private PrintShippingSummaryReportCommandInput commandInput;
    private RecoveredPlasmaShipment mockShipment;
    private ShippingSummaryReport mockReport;
    private ShippingSummaryReportOutput mockReportOutput;

    @BeforeEach
    void setUp() {
        commandInput = new PrintShippingSummaryReportCommandInput(1L, "EMP123", "LOC456");
        mockShipment = mock(RecoveredPlasmaShipment.class);
        mockReport = mock(ShippingSummaryReport.class);
        mockReportOutput = mock(ShippingSummaryReportOutput.class);
    }

    @Test
    @DisplayName("Should successfully print shipping summary report")
    void shouldSuccessfullyPrintShippingSummaryReport() {
        // Arrange
        when(recoveredPlasmaShippingRepository.findOneById(commandInput.shipmentId()))
            .thenReturn(Mono.just(mockShipment));

        when(mockShipment.printShippingSummaryReport(any(PrintShippingSummaryCommand.class),
            eq(cartonRepository),
            eq(systemProcessPropertyRepository),
            eq(recoveredPlasmaShipmentCriteriaRepository),
            eq(locationRepository)))
            .thenReturn(mockReport);

        when(shippingSummaryReportOutputMapper.toOutput(mockReport))
            .thenReturn(mockReportOutput);

        // Act & Assert
        StepVerifier.create(useCase.printShippingSummaryReport(commandInput))
            .assertNext(output -> {
                assertNotNull(output);
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_SUCCESS.getMessage(),
                    output.notifications().get(0).useCaseMessage().message());
                assertEquals(mockReportOutput, output.data());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle shipment not found")
    void shouldHandleShipmentNotFound() {
        // Arrange
        when(recoveredPlasmaShippingRepository.findOneById(commandInput.shipmentId()))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.printShippingSummaryReport(commandInput))
            .assertNext(output -> {
                assertNotNull(output);
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_ERROR.getMessage(),
                    output.notifications().get(0).useCaseMessage().message());
                assertNull(output.data());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle error during report generation")
    void shouldHandleErrorDuringReportGeneration() {
        // Arrange
        when(recoveredPlasmaShippingRepository.findOneById(commandInput.shipmentId()))
            .thenReturn(Mono.just(mockShipment));

        when(mockShipment.printShippingSummaryReport(any(PrintShippingSummaryCommand.class),
            eq(cartonRepository),
            eq(systemProcessPropertyRepository),
            eq(recoveredPlasmaShipmentCriteriaRepository),
            eq(locationRepository)))
            .thenThrow(new RuntimeException("Test error"));

        // Act & Assert
        StepVerifier.create(useCase.printShippingSummaryReport(commandInput))
            .assertNext(output -> {
                assertNotNull(output);
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_ERROR.getMessage(),
                    output.notifications().get(0).useCaseMessage().message());
                assertNull(output.data());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle mapping error")
    void shouldHandleMappingError() {
        // Arrange
        when(recoveredPlasmaShippingRepository.findOneById(commandInput.shipmentId()))
            .thenReturn(Mono.just(mockShipment));

        when(mockShipment.printShippingSummaryReport(any(PrintShippingSummaryCommand.class),
            eq(cartonRepository),
            eq(systemProcessPropertyRepository),
            eq(recoveredPlasmaShipmentCriteriaRepository),
            eq(locationRepository)))
            .thenReturn(mockReport);

        when(shippingSummaryReportOutputMapper.toOutput(mockReport))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        StepVerifier.create(useCase.printShippingSummaryReport(commandInput))
            .assertNext(output -> {
                assertNotNull(output);
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_ERROR.getMessage(),
                    output.notifications().get(0).useCaseMessage().message());
                assertNull(output.data());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle command input validation")
    void shouldHandleCommandInputValidation() {

        when(recoveredPlasmaShippingRepository.findOneById(Mockito.any()))
            .thenReturn(Mono.just(mockShipment));

        // Act & Assert
        StepVerifier.create(useCase.printShippingSummaryReport(mock(PrintShippingSummaryReportCommandInput.class)))
            .assertNext(output -> {
                assertNotNull(output);
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());
                assertEquals(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_ERROR.getMessage(),
                    output.notifications().get(0).useCaseMessage().message());
                assertNull(output.data());
            })
            .verifyComplete();
    }
}

