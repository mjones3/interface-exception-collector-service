package com.arcone.biopro.distribution.irradiation.domain.irradiation.service;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Implementation of out-of-storage validation service.
 */
@Slf4j
public class OutOfStorageValidationServiceImpl implements OutOfStorageValidationService {

    private final BatchRepository batchRepository;
    private final ConfigurationService configurationService;

    public OutOfStorageValidationServiceImpl(BatchRepository batchRepository,
                                           ConfigurationService configurationService) {
        this.batchRepository = batchRepository;
        this.configurationService = configurationService;
    }

    @Override
    public Mono<Void> markEventAsProcessed(String unitNumber, String productCode) {
        return batchRepository.markBatchItemAsTimingRuleValidated(unitNumber, productCode);
    }
    
    @Override
    public Mono<ProcessingResult> processProductStoredEvent(String unitNumber, String productCode, ZonedDateTime storageTime) {
        return batchRepository.findLatestBatchWithItemByUnitAndProduct(unitNumber, productCode)
                .flatMap(batch -> {
                    boolean batchClosed = batch.getEndTime() != null;
                    if (!batchClosed) {
                        return Mono.just(new ProcessingResult(false, false, false, false));
                    }
                    
                    return batchRepository.findBatchItem(batch.getId(), unitNumber, productCode)
                            .flatMap(batchItem -> {
                                boolean alreadyProcessed = Boolean.TRUE.equals(batchItem.isTimingRuleValidated());
                                if (alreadyProcessed) {
                                    return Mono.just(new ProcessingResult(true, true, false, false));
                                }
                                
                                // Validate out-of-storage time
                                String productFamily = batchItem.productFamily();
                                String configKey = "OUT_OF_STORAGE_" + productFamily;
                                ZonedDateTime batchStartTime = batch.getStartTime().atZone(storageTime.getZone());

                                return configurationService.readConfiguration(List.of(configKey))
                                        .next()
                                        .map(config -> {
                                            int timeoutMinutes = Integer.parseInt(config.getValue());
                                            Duration timeDifference = Duration.between(batchStartTime, storageTime);
                                            long minutesDifference = timeDifference.toMinutes();
                                            boolean exceeded = minutesDifference >= timeoutMinutes;
                                            
                                            log.debug("Unit: {}, Product: {}, Product Family: {}, Batch start: {}, Storage time: {}, Difference: {} minutes, Limit: {} minutes, Exceeded: {}",
                                                    unitNumber, productCode, productFamily, batchStartTime, storageTime, minutesDifference, timeoutMinutes, exceeded);

                                            return new ProcessingResult(true, false, exceeded, true);
                                        })
                                        .switchIfEmpty(Mono.just(new ProcessingResult(true, false, false, true)));
                            });
                })
                .switchIfEmpty(Mono.just(new ProcessingResult(false, false, false, false)))
                .onErrorReturn(new ProcessingResult(false, false, false, false));
    }

}
