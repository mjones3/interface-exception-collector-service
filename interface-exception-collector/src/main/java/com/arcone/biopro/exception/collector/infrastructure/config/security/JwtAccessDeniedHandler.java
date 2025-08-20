package com.arcone.biopro.exception.collector.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * JWT Access Denied Handler for handling authorization failures
 * Returns structured error responses when authenticated users lack sufficient
 * privileges
 */
@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "unknown";

        log.warn("[JWT-AUTH] Access denied for user: {} attempting to access: {} {} - {}",
                username, method, requestPath, accessDeniedException.getMessage());

        String requiredRoles = getRequiredRoles(requestPath);
        String currentRoles = getCurrentRoles(authentication);

        JwtErrorResponse errorResponse = JwtErrorResponse.insufficientPrivileges(requestPath, requiredRoles,
                currentRoles);

        response.setStatus(errorResponse.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        log.debug("[JWT-AUTH] Sent access denied response for user: {} ({})", username, errorResponse.getError());
    }

    /**
     * Get required roles based on the requested path
     */
    private String getRequiredRoles(String requestPath) {
        if (requestPath.contains("/api/v1/exceptions")) {
            return "ADMIN or OPERATOR";
        } else if (requestPath.contains("/api/v1/management")) {
            return "ADMIN";
        } else if (requestPath.contains("/api/v1/retry")) {
            return "ADMIN or OPERATOR";
        } else {
            return "appropriate role";
        }
    }

    /**
     * Get current user roles from authentication context
     */
    private String getCurrentRoles(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
        }
        return "none";
    }
}