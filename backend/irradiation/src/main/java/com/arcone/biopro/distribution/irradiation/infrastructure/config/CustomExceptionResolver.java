package com.arcone.biopro.distribution.irradiation.infrastructure.config;

import com.arcone.biopro.distribution.irradiation.domain.exception.CustomBaseException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof CustomBaseException) {
            return buildError((CustomBaseException) ex, env);
        }
        return buildError(new CustomBaseException(ex), env);
    }

    private GraphQLError buildError(CustomBaseException ex,
                                    DataFetchingEnvironment env) {

        log.error("Error occurred at {}: {}", env.getExecutionStepInfo().getPath(), ex.getMessage(), ex.getCause());

        Map<String, Object> extensions = new HashMap<>();
        extensions.put("type", ex.getType());
        extensions.put("classification", ex.getClassification());


        return GraphqlErrorBuilder.newError()
            .message(ex.getMessage())
            .path(env.getExecutionStepInfo().getPath())
            .location(env.getField().getSourceLocation())
            .extensions(extensions)
            .build();
    }
}
