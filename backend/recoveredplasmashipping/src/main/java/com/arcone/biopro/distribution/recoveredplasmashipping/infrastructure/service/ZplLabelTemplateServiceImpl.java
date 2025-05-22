package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonLabel;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LabelTemplateService;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LabelTemplateEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LabelTemplateEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZplLabelTemplateServiceImpl implements LabelTemplateService {

    private final LabelTemplateEntityRepository labelTemplateEntityRepository;

    private final FreemarkerUtils freemarkerUtils;

    @Override
    public Mono<String> processTemplate(String templateName, CartonLabel cartonLabel) {
        var values = cartonLabel.toMap();
        return findTemplate(templateName)
            .flatMap(template -> Mono.just(freemarkerUtils.zpl(template, values)));
    }

    private Mono<LabelTemplateEntity> findTemplate(String templateType) {
        return labelTemplateEntityRepository.findByTypeAndActiveIsTrue(templateType);
    }
}
