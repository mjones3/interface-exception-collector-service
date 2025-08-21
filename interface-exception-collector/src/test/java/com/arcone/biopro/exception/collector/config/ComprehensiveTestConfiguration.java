package com.arcone.biopro.exception.collector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

/**
 * Test configuration for comprehensive test suite.
 * Provides test-specific beans and configurations.
 */
@TestConfiguration
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "logging.level.org.springframework.web=WARN",
        "logging.level.org.springframework.security=WARN",
        "logging.level.org.hibernate=WARN"
})
public class ComprehensiveTestConfiguration {

    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}