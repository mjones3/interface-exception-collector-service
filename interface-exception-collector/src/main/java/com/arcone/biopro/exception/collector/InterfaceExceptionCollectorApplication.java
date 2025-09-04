package com.arcone.biopro.exception.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Main Spring Boot application class for the Interface Exception Collector
 * Service.
 * Enables JPA auditing for automatic timestamp management, Kafka for event
 * processing, and scheduling for periodic tasks.
 * 
 * Configuration fix: Removed spring.profiles.active from application-local.yml
 */
@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")
@EnableKafka
@EnableScheduling
@EnableConfigurationProperties(com.arcone.biopro.exception.collector.infrastructure.config.ApplicationProperties.class)
public class InterfaceExceptionCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterfaceExceptionCollectorApplication.class, args);
    }

    @Bean
    public DateTimeProvider offsetDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}