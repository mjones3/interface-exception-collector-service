package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LabelTemplateEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LabelTemplateEntityRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service.FreemarkerUtils;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service.ZplLabelTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZplLabelTemplateServiceImplTest {

    @Mock
    private LabelTemplateEntityRepository labelTemplateEntityRepository;

    @Mock
    private FreemarkerUtils freemarkerUtils;

    @InjectMocks
    private ZplLabelTemplateServiceImpl zplLabelTemplateService;

    private LabelTemplateEntity mockTemplate;
    private Map<String, Object> mockValues;
    private static final String TEMPLATE_NAME = "TEST_TEMPLATE";
    private static final String PROCESSED_TEMPLATE = "PROCESSED_ZPL_CONTENT";

    @BeforeEach
    void setUp() {
        // Initialize mock template
        mockTemplate = LabelTemplateEntity
            .builder()
            .type(TEMPLATE_NAME)
            .active(true)
            .template("template content")
            .build();


        // Initialize mock values
        mockValues = new HashMap<>();
        mockValues.put("key1", "value1");
        mockValues.put("key2", "value2");
    }

    @Test
    void processTemplate_WhenTemplateExists_ShouldReturnProcessedTemplate() {
        // Arrange
        when(labelTemplateEntityRepository.findByTypeAndActiveIsTrue(TEMPLATE_NAME))
            .thenReturn(Mono.just(mockTemplate));
        when(freemarkerUtils.zpl(any(LabelTemplateEntity.class), any(Map.class)))
            .thenReturn(PROCESSED_TEMPLATE);

        // Act & Assert
        StepVerifier.create(zplLabelTemplateService.processTemplate(TEMPLATE_NAME, mockValues))
            .expectNext(PROCESSED_TEMPLATE)
            .verifyComplete();

        // Verify interactions
        verify(labelTemplateEntityRepository, times(1))
            .findByTypeAndActiveIsTrue(TEMPLATE_NAME);
        verify(freemarkerUtils, times(1))
            .zpl(mockTemplate, mockValues);
    }

    @Test
    void processTemplate_WhenTemplateDoesNotExist_ShouldReturnEmptyMono() {
        // Arrange
        when(labelTemplateEntityRepository.findByTypeAndActiveIsTrue(TEMPLATE_NAME))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(zplLabelTemplateService.processTemplate(TEMPLATE_NAME, mockValues))
            .verifyComplete();

        // Verify interactions
        verify(labelTemplateEntityRepository, times(1))
            .findByTypeAndActiveIsTrue(TEMPLATE_NAME);
        verify(freemarkerUtils, times(0))
            .zpl(any(), any());
    }

    @Test
    void processTemplate_WhenFreemarkerUtilsThrowsException_ShouldPropagateError() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Template processing failed");

        when(labelTemplateEntityRepository.findByTypeAndActiveIsTrue(TEMPLATE_NAME))
            .thenReturn(Mono.just(mockTemplate));
        when(freemarkerUtils.zpl(any(LabelTemplateEntity.class), any(Map.class)))
            .thenThrow(expectedException);

        // Act & Assert
        StepVerifier.create(zplLabelTemplateService.processTemplate(TEMPLATE_NAME, mockValues))
            .expectErrorMatches(throwable ->
                throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Template processing failed"))
            .verify();

        // Verify interactions
        verify(labelTemplateEntityRepository, times(1))
            .findByTypeAndActiveIsTrue(TEMPLATE_NAME);
        verify(freemarkerUtils, times(1))
            .zpl(mockTemplate, mockValues);
    }

    @Test
    void processTemplate_WhenTemplateNameIsNull_ShouldReturnEmptyMono() {
        // Arrange
        when(labelTemplateEntityRepository.findByTypeAndActiveIsTrue(null))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(zplLabelTemplateService.processTemplate(null, mockValues))
            .verifyComplete();

        // Verify interactions
        verify(labelTemplateEntityRepository, times(1))
            .findByTypeAndActiveIsTrue(null);
        verify(freemarkerUtils, times(0))
            .zpl(any(), any());
    }

    @Test
    void processTemplate_WhenValuesMapIsNull_ShouldProcessTemplateWithNullValues() {
        // Arrange
        when(labelTemplateEntityRepository.findByTypeAndActiveIsTrue(TEMPLATE_NAME))
            .thenReturn(Mono.just(mockTemplate));
        when(freemarkerUtils.zpl(any(LabelTemplateEntity.class), any()))
            .thenReturn(PROCESSED_TEMPLATE);

        // Act & Assert
        StepVerifier.create(zplLabelTemplateService.processTemplate(TEMPLATE_NAME, null))
            .expectNext(PROCESSED_TEMPLATE)
            .verifyComplete();

        // Verify interactions
        verify(labelTemplateEntityRepository, times(1))
            .findByTypeAndActiveIsTrue(TEMPLATE_NAME);
        verify(freemarkerUtils, times(1))
            .zpl(mockTemplate, null);
    }
}

