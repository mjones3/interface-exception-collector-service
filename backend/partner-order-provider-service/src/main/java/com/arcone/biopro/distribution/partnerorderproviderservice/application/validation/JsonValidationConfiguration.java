package com.arcone.biopro.distribution.partnerorderproviderservice.application.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
@RequiredArgsConstructor
@Configuration
public class JsonValidationConfiguration implements WebFluxConfigurer {


    private final ObjectMapper objectMapper;

    private final ResourcePatternResolver resourcePatternResolver;

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new JsonSchemaValidatingArgumentResolver(objectMapper, resourcePatternResolver));
        WebFluxConfigurer.super.configureArgumentResolvers(configurer);
    }
}
