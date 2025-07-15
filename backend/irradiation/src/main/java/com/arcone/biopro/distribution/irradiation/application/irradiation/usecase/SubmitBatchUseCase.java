package com.arcone.biopro.distribution.irradiation.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.SubmitBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.mapper.BatchMapper;
import com.arcone.biopro.distribution.irradiation.application.usecase.CommandUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.UseCase;
import com.arcone.biopro.distribution.irradiation.domain.exception.BatchSubmissionException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Use case for submitting a batch of unit numbers for irradiation process.
 */
@Service
public class SubmitBatchUseCase implements CommandUseCase<SubmitBatchCommand, BatchSubmissionResultDTO> {

    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;

    public SubmitBatchUseCase(BatchRepository batchRepository, BatchMapper batchMapper) {
        this.batchRepository = batchRepository;
        this.batchMapper = batchMapper;
    }

    @Override
    public Mono<BatchSubmissionResultDTO> execute(SubmitBatchCommand command) {
        DeviceId deviceId = DeviceId.of(command.deviceId());
        List<BatchItem> batchItems = command.batchItems().stream()
                .map(item -> BatchItem.builder()
                    .unitNumber(new UnitNumber(item.unitNumber()))
                    .productCode(item.productCode())
                    .lotNumber(item.lotNumber())
                    .build())
                .toList();

        return batchRepository.submitBatch(deviceId, command.startTime(), batchItems)
                .map(batchMapper::toSubmissionResult)
                .onErrorMap(throwable -> new BatchSubmissionException("Failed to submit batch: " + throwable.getMessage(), throwable));
    }
}
