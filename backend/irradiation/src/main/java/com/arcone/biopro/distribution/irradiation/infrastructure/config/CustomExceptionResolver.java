package com.arcone.biopro.distribution.irradiation.infrastructure.config;

import com.arcone.biopro.distribution.irradiation.domain.exception.ConfigurationNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

    Map<Class<? extends RuntimeException>, ErrorType> errorTypeMap = new HashMap<>() {{
        put(ConfigurationNotFoundException.class, ErrorType.NOT_FOUND);
        put(IllegalArgumentException.class, ErrorType.BAD_REQUEST);
    }};

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        ErrorType errorType = Objects.requireNonNullElse(errorTypeMap.get(ex.getClass()), ErrorType.INTERNAL_ERROR);

        return GraphqlErrorBuilder.newError()
                .errorType(errorType)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}
