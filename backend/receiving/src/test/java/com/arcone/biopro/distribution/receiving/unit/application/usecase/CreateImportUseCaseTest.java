package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.CreateImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.application.mapper.InputCommandMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.CreateImportUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.CreateImportCommand;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateImportUseCaseTest {

    @Mock
    private ImportRepository importRepository;
    @Mock
    private ImportOutputMapper importOutputMapper;
    @Mock
    private InputCommandMapper inputCommandMapper;
    @Mock
    private ProductConsequenceRepository productConsequenceRepository;
    @Mock
    private DeviceRepository deviceRepository;


    private CreateImportUseCase createImportUseCase;

    @BeforeEach
    void setUp() {
        createImportUseCase = new CreateImportUseCase(
            importRepository,
            importOutputMapper,
            inputCommandMapper,
            productConsequenceRepository,
            deviceRepository
        );


    }

    @Test
    void createImport_WhenSuccessful_ShouldReturnSuccessOutput() {
        // Arrange


        CreateImportCommandInput input = Mockito.mock(CreateImportCommandInput.class);
        CreateImportCommand command = Mockito.mock(CreateImportCommand.class);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getLocationCode()).thenReturn("LOC1");
        when(command.getEmployeeId()).thenReturn("emp-id");


        ImportOutput importOutput = ImportOutput.builder().build();

        when(inputCommandMapper.toCommand(eq(input), eq(productConsequenceRepository), eq(deviceRepository))).thenReturn(command);
        var importMock = Mockito.mock(Import.class);
        when(importMock.getId()).thenReturn(1L);
        when(importRepository.create(any(Import.class))).thenReturn(Mono.just(importMock));
        when(importOutputMapper.toOutput(any(Import.class))).thenReturn(importOutput);

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = createImportUseCase.createImport(input);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {

                Assertions.assertNotNull(output);

                assertNotNull(output);
                assertNotNull(output.data());
                assertEquals(importOutput, output.data());
                assertEquals(1, output.notifications().size());
                assertEquals(UseCaseMessageType.IMPORT_CREATE_SUCCESS.getMessage(),
                    output.notifications().get(0).useCaseMessage().message());
                assertEquals("receiving/1/product-information", output._links().get("next"));

            })
            .verifyComplete();

        verify(inputCommandMapper).toCommand(eq(input), eq(productConsequenceRepository), eq(deviceRepository));
        verify(importRepository).create(any(Import.class));
        verify(importOutputMapper).toOutput(any(Import.class));

    }

    @Test
    void createImport_WhenError_ShouldReturnErrorOutput() {
        // Arrange

        CreateImportCommandInput input = Mockito.mock(CreateImportCommandInput.class);
        CreateImportCommand command = Mockito.mock(CreateImportCommand.class);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getLocationCode()).thenReturn("LOC1");
        when(command.getEmployeeId()).thenReturn("emp-id");

        when(inputCommandMapper.toCommand(eq(input), eq(productConsequenceRepository), eq(deviceRepository))).thenReturn(command);
        when(importRepository.create(any(Import.class))).thenThrow(new RuntimeException("Error creating import"));

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = createImportUseCase.createImport(input);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertNull(output._links());
                assertEquals(1, output.notifications().size());
                assertEquals("Error creating import",
                    output.notifications().get(0).useCaseMessage().message());
                assertEquals(UseCaseNotificationType.WARN,
                    output.notifications().get(0).useCaseMessage().type());
                assertEquals(10,
                    output.notifications().get(0).useCaseMessage().code());
            })
            .verifyComplete();

        verify(inputCommandMapper).toCommand(eq(input), eq(productConsequenceRepository), eq(deviceRepository));
        verify(importRepository).create(any(Import.class));
        verify(importOutputMapper, never()).toOutput(any(Import.class));
    }
}

