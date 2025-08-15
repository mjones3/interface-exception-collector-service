package com.arcone.biopro.partner.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Partner Order Service Application
 * 
 * Provides external API endpoints for partners to submit blood product orders
 * with validation, event publishing, and retry capabilities for the Interface
 * Exception Collector Service.
 */
@SpringBootApplication
@EnableKafka
public class PartnerOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerOrderServiceApplication.class, args);
    }
}