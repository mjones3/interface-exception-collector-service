package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtAuthenticationFilter;
// import com.arcone.biopro.exception.collector.infrastructure.config.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for JWT authentication and role-based access
 * control
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        // private final RateLimitingFilter rateLimitingFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF for stateless API
                                .csrf(AbstractHttpConfigurer::disable)

                                // Configure CORS for both REST and GraphQL endpoints
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Stateless session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configure authorization rules
                                .authorizeHttpRequests(authz -> authz
                                                // Public endpoints - health checks for Kubernetes
                                                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                                                // GraphQL endpoints - require authentication
                                                .requestMatchers("/graphql").authenticated()
                                                .requestMatchers("/subscriptions").authenticated()

                                                // GraphiQL interface - allow in development
                                                .requestMatchers("/graphiql/**").permitAll()

                                                // REST API endpoints with role-based access
                                                .requestMatchers(HttpMethod.GET, "/api/v1/exceptions/**")
                                                .hasAnyRole("OPERATOR", "ADMIN", "VIEWER")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/exceptions")
                                                .hasAnyRole("OPERATOR", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/exceptions/*/retry")
                                                .hasAnyRole("OPERATOR", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/exceptions/*/acknowledge")
                                                .hasAnyRole("OPERATOR", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/exceptions/*/resolve")
                                                .hasAnyRole("OPERATOR", "ADMIN")

                                                // Admin-only endpoints (other actuator endpoints)
                                                .requestMatchers("/actuator/**").hasRole("ADMIN")

                                                // All other requests require authentication
                                                .anyRequest().authenticated())

                                // Add security headers
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions.deny())
                                                .contentTypeOptions(contentTypeOptions -> {
                                                })
                                                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                                                .maxAgeInSeconds(31536000)))

                                // Add custom filters
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Configures CORS for both REST and GraphQL endpoints to allow cross-origin
         * requests
         * from the BioPro Operations Dashboard with enhanced security settings.
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Allow specific origins (configure based on environment)
                // In production, replace with specific dashboard URLs
                configuration.setAllowedOriginPatterns(List.of(
                                "http://localhost:3000", // Development dashboard
                                "https://*.biopro.com", // Production dashboard domains
                                "https://*.arcone.com" // Corporate domains
                ));

                // Allow specific HTTP methods for both REST and GraphQL
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

                // Allow specific headers with security considerations
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-Requested-With",
                                "Accept",
                                "Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers",
                                "X-GraphQL-Operation-Name", // GraphQL operation tracking
                                "X-Apollo-Tracing" // Apollo client tracing
                ));

                // Expose security-related headers to client
                configuration.setExposedHeaders(Arrays.asList(
                                "X-Rate-Limit-Remaining",
                                "X-Rate-Limit-Reset",
                                "X-Request-ID"));

                // Allow credentials for JWT tokens
                configuration.setAllowCredentials(true);

                // Cache preflight requests for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                // Configure CORS for REST API endpoints
                source.registerCorsConfiguration("/api/**", configuration);

                // Configure CORS for GraphQL endpoints
                source.registerCorsConfiguration("/graphql", configuration);
                source.registerCorsConfiguration("/subscriptions", configuration);
                source.registerCorsConfiguration("/graphiql/**", configuration);

                return source;
        }

        /**
         * Configure role hierarchy so that higher roles automatically include lower
         * role permissions.
         * ADMIN > OPERATIONS > VIEWER
         */
        @Bean
        public RoleHierarchy roleHierarchy() {
                RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
                String hierarchy = "ROLE_ADMIN > ROLE_OPERATIONS \n ROLE_OPERATIONS > ROLE_VIEWER";
                roleHierarchy.setHierarchy(hierarchy);
                return roleHierarchy;
        }
}