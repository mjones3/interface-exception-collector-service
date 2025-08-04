package com.arcone.biopro.distribution.irradiation.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.SubmitBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchItemDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.mapper.BatchMapper;
import com.arcone.biopro.distribution.irradiation.application.usecase.CommandUseCase;
import com.arcone.biopro.distribution.irradiation.domain.exception.BatchSubmissionException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Use case for submitting a batch of unit numbers for irradiation process.
 */
@Service
public class SubmitBatchUseCase implements CommandUseCase<SubmitBatchCommand, BatchSubmissionResultDTO> {

    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;
    private final InventoryClient inventoryClient;

    public SubmitBatchUseCase(BatchRepository batchRepository, BatchMapper batchMapper, InventoryClient inventoryClient) {
        this.batchRepository = batchRepository;
        this.batchMapper = batchMapper;
        this.inventoryClient = inventoryClient;
    }

    @Override
    public Mono<BatchSubmissionResultDTO> execute(SubmitBatchCommand command) {
        DeviceId deviceId = DeviceId.of(command.deviceId());

        return Flux.fromIterable(command.batchItems())
                .flatMap(item -> {
                    UnitNumber unitNumber = new UnitNumber(item.unitNumber());
                    return inventoryClient.getInventoryByUnitNumber(unitNumber)
                            .filter(inventory -> item.productCode().equals(inventory.getProductCode()))
                            .next()
                            .map(inventory -> {
                                if (inventory.getIsImported() && isBloodCenterInformationMissing(item)) {
                                    throw new BatchSubmissionException("Blood center information is missing for imported product");
                                }
                                return BatchItem.builder()
                                    .unitNumber(unitNumber)
                                    .productCode(item.productCode())
                                    .lotNumber(item.lotNumber())
                                    .newProductCode(null)
                                    .expirationDate(inventory.getExpirationDate())
                                    .productFamily(inventory.getProductFamily())
                                    .isImported(inventory.getIsImported())
                                    .location(inventory.getLocation().value())
                                    .build();
                            });
                })
                .collectList()
                .flatMap(batchItems -> batchRepository.submitBatch(deviceId, command, batchItems))
                .map(batchMapper::toSubmissionResult)
                .onErrorMap(throwable -> new BatchSubmissionException("Failed to submit batch: " + throwable.getMessage(), throwable));
    }
    private boolean isBloodCenterInformationMissing(BatchItemDTO item) {
        return item.bloodCenterName() == null || item.bloodCenterName().trim().isEmpty() ||
            item.address() == null || item.address().trim().isEmpty() ||
            item.registrationNumber() == null || item.registrationNumber().trim().isEmpty();
    }
}
