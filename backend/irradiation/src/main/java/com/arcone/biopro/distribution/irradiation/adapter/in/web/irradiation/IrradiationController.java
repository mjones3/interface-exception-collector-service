package com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto.BatchSubmissionResult;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.dto.SubmitBatchInput;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.irradiation.mapper.IrradiationGraphQLMapper;
import com.arcone.biopro.distribution.irradiation.application.irradiation.usecase.SubmitBatchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * GraphQL controller for irradiation batch operations.
 */
@Controller
@RequiredArgsConstructor
public class IrradiationController {

    private final SubmitBatchUseCase submitBatchUseCase;
    private final IrradiationGraphQLMapper mapper;

    @MutationMapping
    public Mono<BatchSubmissionResult> submitBatch(@Argument("input") SubmitBatchInput input) {
        return submitBatchUseCase.execute(mapper.toCommand(input))
                .map(mapper::toGraphQLResult);
    }
}