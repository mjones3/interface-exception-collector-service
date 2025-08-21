package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Configuration for structured logging with correlation IDs.
 * Implements structured logging as per requirements US-016, US-017.
 */
@Configuration
@Slf4j
public class LoggingConfig {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String INTERFACE_TYPE_MDC_KEY = "interfaceType";
    public static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    /**
     * Filter to add correlation ID to all HTTP requests.
     */
    @Bean
    public OncePerRequestFilter correlationIdFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {

                String correlationId = extractCorrelationId(request);
                String requestId = UUID.randomUUID().toString();

                try {
                    // Set MDC values for structured logging
                    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
                    MDC.put(REQUEST_ID_MDC_KEY, requestId);

                    // Add correlation ID to response headers
                    response.setHeader(CORRELATION_ID_HEADER, correlationId);
                    response.setHeader("X-Request-ID", requestId);

                    log.debug("Processing request: method={}, uri={}, correlationId={}, requestId={}",
                            request.getMethod(), request.getRequestURI(), correlationId, requestId);

                    filterChain.doFilter(request, response);

                } finally {
                    // Clean up MDC to prevent memory leaks
                    MDC.remove(CORRELATION_ID_MDC_KEY);
                    MDC.remove(REQUEST_ID_MDC_KEY);
                    MDC.remove(USER_ID_MDC_KEY);
                }
            }

            private String extractCorrelationId(HttpServletRequest request) {
                String correlationId = request.getHeader(CORRELATION_ID_HEADER);
                if (correlationId == null || correlationId.trim().isEmpty()) {
                    correlationId = UUID.randomUUID().toString();
                }
                return correlationId;
            }
        };
    }

    /**
     * Utility class for managing logging context.
     */
    public static class LoggingContext {

        /**
         * Sets correlation ID in MDC for current thread.
         */
        public static void setCorrelationId(String correlationId) {
            if (correlationId != null && !correlationId.trim().isEmpty()) {
                MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            }
        }

        /**
         * Sets transaction ID in MDC for current thread.
         */
        public static void setTransactionId(String transactionId) {
            if (transactionId != null && !transactionId.trim().isEmpty()) {
                MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
            }
        }

        /**
         * Sets interface type in MDC for current thread.
         */
        public static void setInterfaceType(String interfaceType) {
            if (interfaceType != null && !interfaceType.trim().isEmpty()) {
                MDC.put(INTERFACE_TYPE_MDC_KEY, interfaceType);
            }
        }

        /**
         * Sets user ID in MDC for current thread.
         */
        public static void setUserId(String userId) {
            if (userId != null && !userId.trim().isEmpty()) {
                MDC.put(USER_ID_MDC_KEY, userId);
            }
        }

        /**
         * Gets correlation ID from MDC.
         */
        public static String getCorrelationId() {
            return MDC.get(CORRELATION_ID_MDC_KEY);
        }

        /**
         * Gets transaction ID from MDC.
         */
        public static String getTransactionId() {
            return MDC.get(TRANSACTION_ID_MDC_KEY);
        }

        /**
         * Clears all logging context from MDC.
         */
        public static void clear() {
            MDC.clear();
        }

        /**
         * Clears specific keys from MDC.
         */
        public static void clearKeys(String... keys) {
            for (String key : keys) {
                MDC.remove(key);
            }
        }

        /**
         * Creates a new correlation ID and sets it in MDC.
         */
        public static String createAndSetCorrelationId() {
            String correlationId = UUID.randomUUID().toString();
            setCorrelationId(correlationId);
            return correlationId;
        }

        /**
         * Executes a runnable with a specific correlation ID.
         */
        public static void withCorrelationId(String correlationId, Runnable runnable) {
            String previousCorrelationId = getCorrelationId();
            try {
                setCorrelationId(correlationId);
                runnable.run();
            } finally {
                if (previousCorrelationId != null) {
                    setCorrelationId(previousCorrelationId);
                } else {
                    MDC.remove(CORRELATION_ID_MDC_KEY);
                }
            }
        }

        /**
         * Executes a runnable with specific transaction and interface context.
         */
        public static void withContext(String correlationId, String transactionId,
                String interfaceType, Runnable runnable) {
            String prevCorrelationId = getCorrelationId();
            String prevTransactionId = getTransactionId();
            String prevInterfaceType = MDC.get(INTERFACE_TYPE_MDC_KEY);

            try {
                setCorrelationId(correlationId);
                setTransactionId(transactionId);
                setInterfaceType(interfaceType);
                runnable.run();
            } finally {
                // Restore previous values
                if (prevCorrelationId != null) {
                    setCorrelationId(prevCorrelationId);
                } else {
                    MDC.remove(CORRELATION_ID_MDC_KEY);
                }

                if (prevTransactionId != null) {
                    setTransactionId(prevTransactionId);
                } else {
                    MDC.remove(TRANSACTION_ID_MDC_KEY);
                }

                if (prevInterfaceType != null) {
                    setInterfaceType(prevInterfaceType);
                } else {
                    MDC.remove(INTERFACE_TYPE_MDC_KEY);
                }
            }
        }
    }
}