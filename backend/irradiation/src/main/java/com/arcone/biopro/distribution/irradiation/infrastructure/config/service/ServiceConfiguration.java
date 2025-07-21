package com.arcone.biopro.distribution.irradiation.infrastructure.config.service;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.LotNumberValidationService;
import com.arcone.biopro.distribution.irradiation.infrastructure.service.LotNumberValidationServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;

/**
 * Configuration class for domain services.
 * Add @Bean methods here for domain service implementations.
 */
@Configuration
public class ServiceConfiguration {

    @Bean
    public LotNumberValidationService lotNumberValidationService(@Qualifier("supplyRSocketRequester") RSocketRequester supplyRSocketRequester) {
        return new LotNumberValidationServiceImpl(supplyRSocketRequester);
    }
}
