package com.arcone.biopro.exception.collector.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the Interface Exception Collector Service.
 * Provides comprehensive API documentation with security schemes and server
 * information.
 */
@Configuration
public class OpenApiConfig {

        @Value("${app.version:1.0.0}")
        private String appVersion;

        @Value("${server.servlet.context-path:}")
        private String contextPath;

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(apiInfo())
                                .servers(List.of(
                                                new Server().url("http://localhost:8080" + contextPath)
                                                                .description("Local Development"),
                                                new Server().url("https://api-dev.biopro.com" + contextPath)
                                                                .description("Development Environment"),
                                                new Server().url("https://api-staging.biopro.com" + contextPath)
                                                                .description("Staging Environment"),
                                                new Server().url("https://api.biopro.com" + contextPath)
                                                                .description("Production Environment")))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("JWT Bearer token authentication")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }

        private Info apiInfo() {
                return new Info()
                                .title("Interface Exception Collector Service API")
                                .description("""
                                                Centralized collection and management of interface exceptions from all BioPro interface services.

                                                ## Features
                                                - Real-time exception collection from Kafka events
                                                - Comprehensive search and filtering capabilities
                                                - Retry management with history tracking
                                                - Critical exception alerting
                                                - Operational dashboards and monitoring

                                                ## Authentication
                                                This API uses JWT Bearer token authentication. Include the token in the Authorization header:
                                                ```
                                                Authorization: Bearer <your-jwt-token>
                                                ```

                                                ## Rate Limiting
                                                API requests are rate-limited to prevent abuse:
                                                - 1000 requests per minute for authenticated users
                                                - 100 requests per minute for unauthenticated requests

                                                ## Error Handling
                                                The API uses standard HTTP status codes and returns error details in a consistent format:
                                                ```json
                                                {
                                                  "timestamp": "2025-08-05T10:30:00Z",
                                                  "status": 400,
                                                  "error": "Bad Request",
                                                  "message": "Invalid request parameters",
                                                  "path": "/api/v1/exceptions"
                                                }
                                                ```
                                                """)
                                .version(appVersion)
                                .contact(new Contact()
                                                .name("BioPro Development Team")
                                                .email("dev-team@biopro.com")
                                                .url("https://biopro.com/support"))
                                .license(new License()
                                                .name("Proprietary")
                                                .url("https://biopro.com/license"));
        }
}