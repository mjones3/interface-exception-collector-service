package com.arcone.biopro.distribution.customer.infrastructure.config;

import com.arcone.biopro.distribution.customer.infrastructure.config.audit.UsernameAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.time.ZonedDateTime;
import java.util.Optional;

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = "auditingDateTimeProvider", auditorAwareRef = "auditorAware")
public class AuditingConfiguration {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        // To enable saving with timeZone
        return () -> Optional.of(ZonedDateTime.now());
    }

    @Bean
    public ReactiveAuditorAware<String> auditorAware() {
        return new UsernameAuditorAware();
    }

}
