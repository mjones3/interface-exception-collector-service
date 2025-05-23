package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface LabelTemplateService {
    Mono<String> processTemplate(String templateName, Map<String, Object> values);
}
