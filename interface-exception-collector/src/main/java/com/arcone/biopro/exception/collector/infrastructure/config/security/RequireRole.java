package com.arcone.biopro.exception.collector.infrastructure.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for role-based access control on GraphQL fields and
 * methods.
 * This annotation can be used to specify the minimum role required to access a
 * field or method.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * The minimum role required to access the annotated element.
     * 
     * @return the required role
     */
    GraphQLSecurityService.Role value();

    /**
     * Optional message to include in security exceptions.
     * 
     * @return the error message
     */
    String message() default "Insufficient permissions";
}