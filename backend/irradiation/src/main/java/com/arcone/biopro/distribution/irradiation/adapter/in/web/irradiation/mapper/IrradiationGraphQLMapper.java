package com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.mapper;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto.BatchSubmissionResult;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto.CompleteBatchInput;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto.SubmitBatchInput;
import com.arcone.biopro.distribution.irradiation.application.irradiation.command.CompleteBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.command.SubmitBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for GraphQL DTOs to application layer conversion.
 */
@Mapper(componentModel = "spring")
public interface IrradiationGraphQLMapper {

    SubmitBatchCommand toCommand(SubmitBatchInput input);

    CompleteBatchCommand toCompleteBatchCommand(CompleteBatchInput input);

    BatchSubmissionResult toGraphQLResult(BatchSubmissionResultDTO dto);
}