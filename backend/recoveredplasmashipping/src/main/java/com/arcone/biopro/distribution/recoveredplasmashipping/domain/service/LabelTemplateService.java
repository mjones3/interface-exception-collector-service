package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonLabel;
import reactor.core.publisher.Mono;

public interface LabelTemplateService {
    Mono<String> processTemplate(String templateName, CartonLabel cartonLabel);
}
