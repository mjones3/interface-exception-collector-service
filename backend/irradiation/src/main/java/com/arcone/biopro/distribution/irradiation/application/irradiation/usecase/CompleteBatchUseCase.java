package com.arcone.biopro.distribution.irradiation.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.CompleteBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.application.usecase.CommandUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.BatchCompletionService;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CompleteBatchUseCase implements CommandUseCase<CompleteBatchCommand, BatchSubmissionResultDTO> {

    private final BatchCompletionService batchCompletionService;
    private final BatchRepository batchRepository;

    public CompleteBatchUseCase(BatchCompletionService batchCompletionService, BatchRepository batchRepository) {
        this.batchCompletionService = batchCompletionService;
        this.batchRepository = batchRepository;
    }

    @Override
    public Mono<BatchSubmissionResultDTO> execute(CompleteBatchCommand command) {
        DeviceId deviceId = DeviceId.of(command.deviceId());

        var itemCompletions = command.batchItems().stream()
                .map(dto -> new IrradiationAggregate.BatchItemCompletion(
                    dto.unitNumber(), dto.productCode(), dto.isIrradiated()))
                .toList();

        return batchRepository.findActiveBatchByDeviceId(deviceId)
                .switchIfEmpty(Mono.error(new RuntimeException("No active batch found for device: " + command.deviceId())))
                .flatMap(batch -> batchCompletionService.prepareBatchCompletion(
                    batch.getId(), itemCompletions, command.endTime())
                    .flatMap(aggregate -> batchCompletionService.completeBatch(aggregate)
                        .thenReturn(buildSuccessResult(batch.getId().getValue()))));
    }

    private BatchSubmissionResultDTO buildSuccessResult(Long batchId) {
        return BatchSubmissionResultDTO.builder()
                .batchId(batchId)
                .message("Batch completed successfully")
                .success(true)
                .build();
    }
}
