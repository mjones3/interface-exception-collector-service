package com.arcone.biopro.exception.collector.infrastructure.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor to handle mutation timeouts and provide timeout management
 * for GraphQL mutations.
 */
@Component
public class MutationTimeoutInterceptor implements HandlerInterceptor {

    private final long timeoutMillis;

    public MutationTimeoutInterceptor() {
        this.timeoutMillis = 30000; // 30 seconds default
    }

    public MutationTimeoutInterceptor(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Set timeout attribute for the request
        request.setAttribute("mutation.timeout", timeoutMillis);
        return true;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }
}