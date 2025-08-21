package com.arcone.biopro.exception.collector.api.graphql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;

/**
 * Configuration for Bean Validation in GraphQL operations.
 * Enables method-level validation and custom validators.
 */
@Configuration
public class ValidationConfig {

    /**
     * Creates a validator factory bean for Bean Validation.
     */
    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * Creates a validator bean for manual validation.
     */
    @Bean
    public Validator validator() {
        return validatorFactory();
    }

    /**
     * Enables method-level validation for GraphQL resolvers.
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validatorFactory());
        return processor;
    }
}