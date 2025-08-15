package com.arcone.biopro.exception.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
 */
@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")
@EnableKafka
@EnableScheduling
public class InterfaceExceptionCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterfaceExceptionCollectorApplication.class, args);
    }

    @Bean
    public DateTimeProvider offsetDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}