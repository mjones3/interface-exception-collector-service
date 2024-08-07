package com.arcone.biopro.distribution.inventory.adapter.in.web.controller.errors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The implementation of this interface is generally used to handle exceptions
 * occurring prior to the ExceptionHandler selection in spring-web lifecycle.
 * see https://github.com/spring-projects/spring-framework/issues/22991
 */
public interface ExceptionTranslation {
    /**
     * Method to translate an Exception to ProblemDetail.
     *
     * @param ex      The exception that needs to be handled
     * @param request The request that is being served
     * @return Returns the Responseentity containing the ProblemDetail
     */
    public Mono<ResponseEntity<Object>> handleAnyException(Throwable ex, ServerWebExchange request);
}
