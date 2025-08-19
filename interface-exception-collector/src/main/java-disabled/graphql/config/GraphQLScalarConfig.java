package com.arcone.biopro.exception.collector.api.graphql.config;

import graphql.scalars.ExtendedScalars;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * GraphQL scalar type configuration for custom data types.
 * Registers custom scalar types like DateTime, JSON, and numeric types
 * for use in GraphQL schema definitions.
 */
@Configuration
@Slf4j
public class GraphQLScalarConfig {

    /**
     * Configures custom GraphQL scalar types.
     * Registers DateTime, JSON, and other extended scalar types.
     *
     * @return RuntimeWiringConfigurer with custom scalar registrations
     */
    @Bean
    public RuntimeWiringConfigurer scalarWiringConfigurer() {
        return wiringBuilder -> {
            log.info("Registering custom GraphQL scalar types");

            // Register DateTime scalar for handling ISO-8601 date/time values
            wiringBuilder.scalar(ExtendedScalars.DateTime);
            log.debug("Registered DateTime scalar type");

            // Register JSON scalar for handling arbitrary JSON objects
            wiringBuilder.scalar(ExtendedScalars.Json);
            log.debug("Registered JSON scalar type");

            // Register Long scalar for handling large integer values
            wiringBuilder.scalar(ExtendedScalars.GraphQLLong);
            log.debug("Registered Long scalar type");

            // Register PositiveInt scalar for validation
            wiringBuilder.scalar(ExtendedScalars.PositiveInt);
            log.debug("Registered PositiveInt scalar type");

            // Register NonNegativeInt scalar for validation
            wiringBuilder.scalar(ExtendedScalars.NonNegativeInt);
            log.debug("Registered NonNegativeInt scalar type");

            // Register URL scalar for URL validation
            wiringBuilder.scalar(ExtendedScalars.Url);
            log.debug("Registered URL scalar type");

            log.info("Custom GraphQL scalar types registered successfully");
        };
    }
}