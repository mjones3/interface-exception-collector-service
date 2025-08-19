package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * GraphQL Security configuration for JWT authentication and role-based access
 * control.
 * Configures Spring Security for GraphQL endpoint protection with JWT
 * authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class GraphQLSecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        /**
         * Configures the security filter chain for GraphQL endpoints.
         * Sets up JWT authentication, role-based access control, and CORS
         * configuration.
         */
        @Bean
        public SecurityFilterChain graphqlSecurityFilterChain(HttpSecurity http) throws Exception {
                return http
                                // Disable CSRF for stateless GraphQL API
                                .csrf(AbstractHttpConfigurer::disable)

                                // Configure CORS for GraphQL endpoints
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Stateless session management for JWT
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configure authorization rules
                                .authorizeHttpRequests(authz -> authz
                                                // Public endpoints - health checks and documentation
                                                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                                                // GraphQL endpoints - require authentication
                                                .requestMatchers("/graphql").authenticated()
                                                .requestMatchers("/subscriptions").authenticated()

                                                // GraphiQL interface - allow in development
                                                .requestMatchers("/graphiql/**").permitAll()

                                                // REST API endpoints with role-based access
                                                .requestMatchers(HttpMethod.GET, "/api/v1/exceptions/**")
                                                .hasAnyRole("ADMIN", "OPERATIONS", "VIEWER")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/exceptions")
                                                .hasAnyRole("ADMIN", "OPERATIONS")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/exceptions/*/retry")
                                                .hasAnyRole("ADMIN", "OPERATIONS")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/exceptions/*/acknowledge")
                                                .hasAnyRole("ADMIN", "OPERATIONS")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/exceptions/*/resolve")
                                                .hasAnyRole("ADMIN", "OPERATIONS")

                                                // Admin-only endpoints
                                                .requestMatchers("/actuator/**").hasRole("ADMIN")

                                                // All other requests require authentication
                                                .anyRequest().authenticated())

                                // Add security headers
                                .headers(headers -> headers
                                                .frameOptions().deny()
                                                .contentTypeOptions().and()
                                                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                                                .maxAgeInSeconds(31536000))
                                                .and())

                                // Add JWT authentication filter
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        /**
         * Configures CORS for GraphQL endpoints to allow cross-origin requests
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

                // Allow specific HTTP methods for GraphQL
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));

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
                source.registerCorsConfiguration("/graphql", configuration);
                source.registerCorsConfiguration("/subscriptions", configuration);
                source.registerCorsConfiguration("/graphiql/**", configuration);

                return source;
        }
}