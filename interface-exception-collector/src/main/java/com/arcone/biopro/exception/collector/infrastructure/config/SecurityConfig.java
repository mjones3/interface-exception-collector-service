package com.arcone.biopro.exception.collector.config;

import com.arcone.biopro.exception.collector.config.security.JwtAuthenticationFilter;
// import com.arcone.biopro.exception.collector.config.security.RateLimitingFilter;
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

                                // Stateless session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configure authorization rules
                                .authorizeHttpRequests(authz -> authz
                                                // Public endpoints - health checks for Kubernetes
                                                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                                                // API endpoints with role-based access
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

                                // Add custom filters
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}