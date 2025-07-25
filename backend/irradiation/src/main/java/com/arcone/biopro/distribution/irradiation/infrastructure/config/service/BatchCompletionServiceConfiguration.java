package com.arcone.biopro.distribution.irradiation.infrastructure.config.service;


import com.arcone.biopro.distribution.irradiation.domain.event.IrradiationEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.BatchCompletionService;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.BatchCompletionServiceImpl;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.domain.service.ProductDeterminationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchCompletionServiceConfiguration {
    
    @Bean
    public BatchCompletionService batchCompletionService(
            BatchRepository batchRepository,
            DeviceRepository deviceRepository,
            ProductDeterminationService productDeterminationService,
            IrradiationEventPublisher eventPublisher,
            ConfigurationService configurationService) {
        
        return new BatchCompletionServiceImpl(
            batchRepository,
            deviceRepository, 
            productDeterminationService,
            eventPublisher,
            configurationService
        );
    }
}