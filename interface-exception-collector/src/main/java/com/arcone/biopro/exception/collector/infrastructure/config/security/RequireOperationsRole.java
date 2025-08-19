package com.arcone.biopro.exception.collector.infrastructure.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for operations-level access control on GraphQL mutations.
 * This annotation indicates that the annotated method requires OPERATIONS or
 * ADMIN role.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOperationsRole {

    /**
     * Optional message to include in security exceptions.
     * 
     * @return the error message
     */
    String message() default "Operations role required for this action";
}