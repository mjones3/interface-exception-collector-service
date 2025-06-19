package com.arcone.biopro.distribution.order.infrastructure.config;

import com.arcone.biopro.distribution.order.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.order.infrastructure.controller.error.DataNotFoundException;
import com.arcone.biopro.distribution.order.infrastructure.controller.error.ServiceNotAvailableException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import static java.util.Optional.ofNullable;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(ExtendedScalars.DateTime)
            .scalar(ExtendedScalars.Date)
            .scalar(ExtendedScalars.LocalTime)
            .scalar(ExtendedScalars.Json);
    }

    @Bean
    DataFetcherExceptionResolverAdapter dataFetcherExceptionResolverAdapter() {
        return new DataFetcherExceptionResolverAdapter() {
            @Override
            protected GraphQLError resolveToSingleError(Throwable throwable, DataFetchingEnvironment env) {
                return switch (throwable) {
                    case DataNotFoundException e -> this.buildGraphQLErrorFor(ErrorType.NOT_FOUND, e, env);
                    case IllegalArgumentException e -> this.buildGraphQLErrorFor(ErrorType.BAD_REQUEST, e, env);
                    case NoResultsFoundException e -> this.buildGraphQLErrorFor(ErrorType.NOT_FOUND, e, env);
                    case ServiceNotAvailableException e -> this.buildGraphQLErrorFor(ErrorType.INTERNAL_ERROR, e, env);
                    default -> super.resolveToSingleError(throwable, env);
                };
            }

            private GraphQLError buildGraphQLErrorFor(ErrorType type, Throwable e, DataFetchingEnvironment env) {
                return GraphqlErrorBuilder.newError()
                    .errorType(type)
                    .message(e.getMessage())
                    .path(getResultPathOrNull(env))
                    .build();
            }

            private ResultPath getResultPathOrNull(DataFetchingEnvironment env) {
                return ofNullable(env)
                    .map(DataFetchingEnvironment::getExecutionStepInfo)
                    .map(ExecutionStepInfo::getPath)
                    .orElse(null);
            }
        };
    }

}
