package com.arcone.biopro.exception.collector.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Aspect for handling custom security annotations in GraphQL resolvers.
 * This aspect intercepts method calls annotated with security annotations
 * and performs authorization checks before allowing method execution.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class GraphQLSecurityAspect {

    private final GraphQLSecurityService securityService;

    /**
     * Intercepts methods annotated with @RequireRole and performs role-based
     * authorization.
     *
     * @param joinPoint   the method execution join point
     * @param requireRole the role requirement annotation
     * @return the method result if authorized
     * @throws Throwable if method execution fails or access is denied
     */
    @Around("@annotation(requireRole)")
    public Object checkRequiredRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("Checking role requirement {} for method {}.{}",
                requireRole.value(), className, methodName);

        if (!securityService.hasAnyRole(requireRole.value())) {
            String username = securityService.getCurrentUsername();
            log.warn("Access denied for user {} to method {}.{} - required role: {}",
                    username, className, methodName, requireRole.value());

            throw new AccessDeniedException(requireRole.message());
        }

        log.debug("Role check passed for user {} accessing method {}.{}",
                securityService.getCurrentUsername(), className, methodName);

        return joinPoint.proceed();
    }

    /**
     * Intercepts methods annotated with @RequireOperationsRole and performs
     * operations-level authorization.
     *
     * @param joinPoint             the method execution join point
     * @param requireOperationsRole the operations role requirement annotation
     * @return the method result if authorized
     * @throws Throwable if method execution fails or access is denied
     */
    @Around("@annotation(requireOperationsRole)")
    public Object checkOperationsRole(ProceedingJoinPoint joinPoint, RequireOperationsRole requireOperationsRole)
            throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("Checking operations role requirement for method {}.{}", className, methodName);

        if (!securityService.canPerformOperations()) {
            String username = securityService.getCurrentUsername();
            log.warn("Access denied for user {} to method {}.{} - operations role required",
                    username, className, methodName);

            throw new AccessDeniedException(requireOperationsRole.message());
        }

        log.debug("Operations role check passed for user {} accessing method {}.{}",
                securityService.getCurrentUsername(), className, methodName);

        return joinPoint.proceed();
    }
}