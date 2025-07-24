package com.arcone.biopro.distribution.irradiation.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProductInput;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.ProductModifiedProducer;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.QuarantineProductProducer;
import com.arcone.biopro.distribution.irradiation.application.irradiation.command.CompleteBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchItemCompletionDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.application.usecase.CommandUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.domain.service.ProductDeterminationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CompleteBatchUseCase implements CommandUseCase<CompleteBatchCommand, BatchSubmissionResultDTO> {

    private final BatchRepository batchRepository;
    private final DeviceRepository deviceRepository;
    private final ProductDeterminationService productDeterminationService;
    private final ProductModifiedProducer productModifiedProducer;
    private final QuarantineProductProducer quarantineProductProducer;
    private final ConfigurationService configurationService;

    public CompleteBatchUseCase(BatchRepository batchRepository, DeviceRepository deviceRepository,
                               ProductDeterminationService productDeterminationService,
                               ProductModifiedProducer productModifiedProducer, QuarantineProductProducer quarantineProductProducer,
                               ConfigurationService configurationService) {
        this.batchRepository = batchRepository;
        this.deviceRepository = deviceRepository;
        this.productDeterminationService = productDeterminationService;
        this.productModifiedProducer = productModifiedProducer;
        this.quarantineProductProducer = quarantineProductProducer;
        this.configurationService = configurationService;
    }

    @Override
    public Mono<BatchSubmissionResultDTO> execute(CompleteBatchCommand command) {
        try {
            BatchId batchId = BatchId.of(Long.parseLong(command.batchId()));

            return batchRepository.findById(batchId)
                    .switchIfEmpty(Mono.error(new RuntimeException("Batch not found")))
                    .flatMap(batch -> deviceRepository.findByDeviceId(batch.getDeviceId())
                            .flatMap(device -> completeBatchProcessing(command, batchId, device.getLocation().value())))
                    .map(this::buildSuccessResult);
        } catch (NumberFormatException e) {
            return Mono.error(new RuntimeException("Invalid batch ID format"));
        }
    }

    private Mono<String> completeBatchProcessing(CompleteBatchCommand command, BatchId batchId, String deviceLocation) {
        var irradiatedItems = command.batchItems().stream().filter(BatchItemCompletionDTO::isIrradiated).toList();
        var nonIrradiatedItems = command.batchItems().stream().filter(item -> !item.isIrradiated()).toList();

        return processIrradiatedItems(irradiatedItems, batchId, deviceLocation)
                .then(batchRepository.completeBatch(batchId, command.endTime()))
                .then(publishQuarantineEvents(nonIrradiatedItems))
                .thenReturn(command.batchId());
    }

    private Mono<Void> processIrradiatedItems(List<BatchItemCompletionDTO> items, BatchId batchId, String deviceLocation) {
        if (items.isEmpty()) {
            return Mono.empty();
        }

        var uniqueProductCodes = items.stream()
                .map(item -> new ProductCode(item.productCode()))
                .distinct()
                .toList();

        return Flux.fromIterable(uniqueProductCodes)
                .flatMap(productDeterminationService::findProductDetermination)
                .collectMap(determination -> determination.getSourceProductCode().value())
                .flatMap(determinations -> Flux.fromIterable(items)
                        .flatMap(item -> {
                            var determination = determinations.get(item.productCode());
                            if (determination == null) {
                                return Mono.error(new RuntimeException("No product determination found for: " + item.productCode()));
                            }

                            return batchRepository.findBatchItem(batchId, item.unitNumber(), item.productCode())
                                    .flatMap(batchItem ->
                                        batchRepository.updateBatchItemNewProductCode(batchId, item.unitNumber(), item.productCode(), determination.getTargetProductCode().value())
                                                .then(publishProductModifiedEvent(item, determination, batchItem, deviceLocation))
                                    );
                        })
                        .then());
    }

    private Mono<Void> publishProductModifiedEvent(BatchItemCompletionDTO item,
                                                   ProductDetermination determination,
                                                   BatchItem batchItem,
                                                   String deviceLocation) {
        return determineFormattedExpirationDate(batchItem.expirationDate())
            .flatMap(formattedExpirationDate -> {
                var productModified = new ProductModified(
                    item.unitNumber(),
                    determination.getTargetProductCode().value(),
                    determination.getTargetProductDescription(),
                    item.productCode(),
                    batchItem.productFamily(),
                    formattedExpirationDate,
                    "23:59",
                    deviceLocation
                );

                return productModifiedProducer.publishProductModified(productModified);
            });
    }

    private Mono<Void> publishQuarantineEvents(List<BatchItemCompletionDTO> nonIrradiatedItems) {
        if (nonIrradiatedItems.isEmpty()) {
            return Mono.empty();
        }

        var quarantineProducts = nonIrradiatedItems.stream()
                .map(item -> new QuarantineProductInput(item.unitNumber(), item.productCode()))
                .toList();

        var quarantinePayload = new QuarantineProduct(
                quarantineProducts,
                "IRRADIATION_SYSTEM",
                "IRRADIATION_INCOMPLETE",
                "Products not irradiated during batch processing",
                "IRRADIATION_SYSTEM"
        );

        return quarantineProductProducer.publishQuarantineProduct(quarantinePayload);
    }

    private BatchSubmissionResultDTO buildSuccessResult(String batchId) {
        return BatchSubmissionResultDTO.builder()
                .batchId(Long.parseLong(batchId))
                .message("Batch completed successfully")
                .success(true)
                .build();
    }



    /**
     * Determines the formatted expiration date based on the shorter of:
     * 1. The original expiration date from the batch item
     * 2. The calculated expiration date (28 days from now at 23:59)
     *
     * @param originalExpirationDate The original expiration date from the batch item
     * @return Mono<String> Formatted expiration date string in MM/dd/yyyy format
     */
    private Mono<String> determineFormattedExpirationDate(LocalDateTime originalExpirationDate) {
        return getConfiguredExpirationDays()
            .map(expirationDays -> {
                // Calculate the new expiration date based on irradiation
                LocalDateTime calculatedExpirationDate = LocalDateTime.now()
                    .plusDays(expirationDays)
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(0)
                    .withNano(0);

                // Use the earlier of the two dates
                LocalDateTime finalExpirationDate = originalExpirationDate != null &&
                    originalExpirationDate.isBefore(calculatedExpirationDate) ?
                    originalExpirationDate : calculatedExpirationDate;

                // Format the date for the event
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
