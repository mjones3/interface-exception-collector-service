package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.AddImportItemCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.application.mapper.InputCommandMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.AddImportItemUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.AddImportItemCommand;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddImportItemUseCaseTest {

    @Mock
    private ImportRepository importRepository;

    @Mock
    private ImportOutputMapper importOutputMapper;

    @Mock
    private InputCommandMapper inputCommandMapper;

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private AddImportItemUseCase useCase;

    @Test
    void createImportItem_WhenSuccessful_ShouldReturnSuccessOutput() {
        // Arrange
        Long importId = 123L;
        AddImportItemCommandInput commandInput = AddImportItemCommandInput.builder().build();
        Import mockImport = mock(Import.class);
        AddImportItemCommand createCommand = mock(AddImportItemCommand.class);
        ImportItem createdItem = mock(ImportItem.class);
        ImportOutput importOutput = mock(ImportOutput.class);

        when(importRepository.findOneById(any())).thenReturn(Mono.just(mockImport));
        when(inputCommandMapper.toCommand(any())).thenReturn(createCommand);

        when(mockImport.createImportItem(any(), any(ConfigurationService.class), any(ProductConsequenceRepository.class))).thenReturn(createdItem);

        when(importRepository.createImportItem(any())).thenReturn(Mono.just(createdItem));
        when(createdItem.getImportId()).thenReturn(importId);
        when(importRepository.findOneById(any())).thenReturn(Mono.just(mockImport));
        when(importOutputMapper.toOutput(any(Import.class))).thenReturn(importOutput);

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = useCase.createImportItem(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertEquals(importOutput, output.data());
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.IMPORT_ITEM_CREATE_SUCCESS.getMessage(),
                    notification.useCaseMessage().message());
                assertEquals(UseCaseMessageType.IMPORT_ITEM_CREATE_SUCCESS.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(UseCaseMessageType.IMPORT_ITEM_CREATE_SUCCESS.getType(),
                    notification.useCaseMessage().type());
            })
            .verifyComplete();
    }

    @Test
    void createImportItem_WhenImportNotFound_ShouldReturnError() {
        // Arrange
        Long importId = 1L;
        AddImportItemCommandInput commandInput = AddImportItemCommandInput.builder()
            .importId(importId)
            .build();

        when(importRepository.findOneById(Mockito.eq(importId))).thenReturn(Mono.empty());

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = useCase.createImportItem(commandInput);

        // Assert
        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof DomainNotFoundForKeyException &&
                    throwable.getMessage().contains("1"))
            .verify();
    }

    @Test
    void createImportItem_WhenErrorOccurs_ShouldReturnErrorOutput() {
        // Arrange
        Long importId = 123L;
        String errorMessage = "Error creating import item";
        AddImportItemCommandInput commandInput = AddImportItemCommandInput.builder()
            .importId(importId)
            .build();
        Import existingImport = mock(Import.class);

        when(importRepository.findOneById(importId))
            .thenReturn(Mono.just(existingImport));
        when(inputCommandMapper.toCommand(commandInput))
            .thenThrow(new RuntimeException(errorMessage));

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = useCase.createImportItem(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(errorMessage, notification.useCaseMessage().message());
                assertEquals(11, notification.useCaseMessage().code());
                assertEquals(UseCaseNotificationType.WARN, notification.useCaseMessage().type());
            })
            .verifyComplete();
    }

    @Test
    void createImportItem_WhenCreateImportItemFails_ShouldReturnErrorOutput() {
        // Arrange
        Long importId = 123L;
        AddImportItemCommandInput commandInput = AddImportItemCommandInput.builder()
            .importId(importId)
            .build();
        Import existingImport = mock(Import.class);
        AddImportItemCommand createCommand = mock(AddImportItemCommand.class);
        ImportItem createdItem = mock(ImportItem.class);

        when(importRepository.findOneById(importId)).thenReturn(Mono.just(existingImport));
        when(inputCommandMapper.toCommand(commandInput)).thenReturn(createCommand);
        when(existingImport.createImportItem(createCommand, configurationService, productConsequenceRepository)).thenReturn(createdItem);
        when(importRepository.createImportItem(createdItem))
            .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = useCase.createImportItem(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertNotNull(output.notifications());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals("Database error", notification.useCaseMessage().message());
                assertEquals(11, notification.useCaseMessage().code());
                assertEquals(UseCaseNotificationType.WARN, notification.useCaseMessage().type());
            })
            .verifyComplete();
    }
}

