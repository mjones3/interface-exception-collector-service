package com.arcone.biopro.distribution.irradiation.infrastructure.config;

import com.arcone.biopro.distribution.irradiation.domain.exception.ConfigurationNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {


    Map<Class<? extends RuntimeException>, ErrorType> errorTypeMap = new HashMap<>() {{
            put(IllegalArgumentException.class, ErrorType.BAD_REQUEST);
            put(ConfigurationNotFoundException.class, ErrorType.NOT_FOUND);

        }};

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        
        Throwable rootCause = getRootCause(ex);
        ErrorType errorType = Objects.requireNonNullElse(errorTypeMap.get(rootCause.getClass()), ErrorType.INTERNAL_ERROR);

        return GraphqlErrorBuilder.newError()
            .errorType(errorType)
            .message(rootCause.getMessage())
            .path(env.getExecutionStepInfo().getPath())
            .location(env.getField().getSourceLocation())
            .build();
    }
    
    private Throwable getRootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
