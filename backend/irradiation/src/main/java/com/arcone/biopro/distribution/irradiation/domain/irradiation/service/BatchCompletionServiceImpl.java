package com.arcone.biopro.distribution.irradiation.domain.irradiation.service;

import com.arcone.biopro.distribution.irradiation.domain.event.IrradiationEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.event.ProductModifiedEvent;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.domain.service.ProductDeterminationService;
import com.arcone.biopro.distribution.irradiation.domain.event.ProductQuarantinedEvent;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProductInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BatchCompletionServiceImpl implements BatchCompletionService {

    private final BatchRepository batchRepository;
    private final DeviceRepository deviceRepository;
    private final ProductDeterminationService productDeterminationService;
    private final IrradiationEventPublisher eventPublisher;
    private final ConfigurationService configurationService;
    public BatchCompletionServiceImpl(BatchRepository batchRepository,
                                     DeviceRepository deviceRepository,
                                     ProductDeterminationService productDeterminationService,
                                     IrradiationEventPublisher eventPublisher,
                                     ConfigurationService configurationService) {
        this.batchRepository = batchRepository;
        this.deviceRepository = deviceRepository;
        this.productDeterminationService = productDeterminationService;
        this.eventPublisher = eventPublisher;
        this.configurationService = configurationService;
    }

    @Override
    public Mono<IrradiationAggregate> prepareBatchCompletion(
            BatchId batchId,
            List<IrradiationAggregate.BatchItemCompletion> itemCompletions,
            LocalDateTime completionTime) {

        return batchRepository.findById(batchId)
                .switchIfEmpty(Mono.error(new RuntimeException("Batch not found")))
                .flatMap(batch -> deviceRepository.findByDeviceId(batch.getDeviceId())
                        .map(device -> new IrradiationAggregate(
                            device,
                            batch,
                            itemCompletions,
                            productDeterminationService,
                            completionTime
                        ))
                )
                .flatMap(aggregate ->
                    aggregate.loadProductDeterminations()
                            .map(determinations -> new IrradiationAggregate(
                                aggregate.getDevice(),
                                aggregate.getBatch(),
                                aggregate.getItemCompletions(),
                                determinations,
                                aggregate.getCompletionTime()
                            ))
                )
                .doOnNext(IrradiationAggregate::validateBatchCompletion);
    }

    @Override
    public Mono<Void> completeBatch(IrradiationAggregate aggregate) {
        return processIrradiatedItems(aggregate)
                .then(batchRepository.completeBatch(aggregate.getBatch().getId(), aggregate.getCompletionTime()))
                .then(publishQuarantineEvents(aggregate.getNonIrradiatedItems()))
                .then();
    }



    private Mono<Void> processIrradiatedItems(IrradiationAggregate aggregate) {
        return Flux.fromIterable(aggregate.getIrradiatedItems())
                .flatMap(completion ->
                    batchRepository.findBatchItem(aggregate.getBatch().getId(), completion.unitNumber(), completion.productCode())
                        .map(originalItem -> aggregate.processIrradiatedItem(completion, originalItem))
                        .flatMap(updatedItem ->
                            batchRepository.updateBatchItemNewProductCode(
                                aggregate.getBatch().getId(),
                                completion.unitNumber(),
                                completion.productCode(),
                                updatedItem.newProductCode()
                            ).then(publishProductModifiedEvent(completion, aggregate, updatedItem))
                        )
                )
                .then();
    }

    private Mono<Void> publishProductModifiedEvent(
            IrradiationAggregate.BatchItemCompletion completion,
            IrradiationAggregate aggregate,
            com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem updatedItem) {

        return determineFormattedExpirationDate(updatedItem.expirationDate())
            .flatMap(formattedExpirationDate -> {
                var productModified = new com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified(
                    completion.unitNumber(),
                    updatedItem.newProductCode(),
                    updatedItem.productDescription(),
                    completion.productCode(),
                    updatedItem.productFamily(),
                    formattedExpirationDate,
                    "23:59",
                    aggregate.getDevice().getLocation().value()
                );

                eventPublisher.publish(new ProductModifiedEvent(productModified));
                return Mono.empty();
            });
    }

    private Mono<Void> publishQuarantineEvents(List<IrradiationAggregate.BatchItemCompletion> nonIrradiatedItems) {
        if (nonIrradiatedItems.isEmpty()) {
            return Mono.empty();
        }

        var quarantineProduct = new QuarantineProduct(
                nonIrradiatedItems.stream()
                        .map(item -> new QuarantineProductInput(item.unitNumber(), item.productCode()))
                        .toList(),
                "IRRADIATION_SYSTEM",
                "IRRADIATION_INCOMPLETE",
                "Products not irradiated during batch processing",
                "IRRADIATION_SYSTEM"
        );

        eventPublisher.publish(new ProductQuarantinedEvent(quarantineProduct));
        return Mono.empty();
    }

    private Mono<String> determineFormattedExpirationDate(LocalDateTime originalExpirationDate) {
        return getConfiguredExpirationDays()
            .map(expirationDays -> {
                LocalDateTime calculatedExpirationDate = LocalDateTime.now()
                    .plusDays(expirationDays)
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(0)
                    .withNano(0);

                LocalDateTime finalExpirationDate = originalExpirationDate != null &&
                    originalExpirationDate.isBefore(calculatedExpirationDate) ?
                    originalExpirationDate : calculatedExpirationDate;

                return finalExpirationDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            });
    }

    private Mono<Integer> getConfiguredExpirationDays() {
        return configurationService.readConfiguration(List.of("IRRADIATION_EXPIRATION_DAYS"))
            .map(config -> Integer.parseInt(config.getValue()))
            .next()
            .defaultIfEmpty(28);
    }


}
