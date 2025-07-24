package com.arcone.biopro.distribution.irradiation.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.CompleteBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.application.usecase.CommandUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.BatchCompletionService;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CompleteBatchUseCase implements CommandUseCase<CompleteBatchCommand, BatchSubmissionResultDTO> {

    private final BatchCompletionService batchCompletionService;

    public CompleteBatchUseCase(BatchCompletionService batchCompletionService) {
        this.batchCompletionService = batchCompletionService;
    }

    @Override
    public Mono<BatchSubmissionResultDTO> execute(CompleteBatchCommand command) {
        try {
            BatchId batchId = BatchId.of(Long.parseLong(command.batchId()));

            var itemCompletions = command.batchItems().stream()
                    .map(dto -> new IrradiationAggregate.BatchItemCompletion(
                        dto.unitNumber(), dto.productCode(), dto.isIrradiated()))
                    .toList();

            return batchCompletionService.prepareBatchCompletion(batchId, itemCompletions, command.endTime())
                    .flatMap(batchCompletionService::completeBatch)
                    .thenReturn(buildSuccessResult(command.batchId()));

        } catch (NumberFormatException e) {
            return Mono.error(new RuntimeException("Invalid batch ID format"));
        }
    }

    private BatchSubmissionResultDTO buildSuccessResult(String batchId) {
        return BatchSubmissionResultDTO.builder()
                .batchId(Long.parseLong(batchId))
                .message("Batch completed successfully")
                .success(true)
                .build();
    }
}
