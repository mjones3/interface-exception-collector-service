package com.arcone.biopro.distribution.irradiation.infrastructure.config;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
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
            put(DeviceValidationFailureException.class, ErrorType.BAD_REQUEST);
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


