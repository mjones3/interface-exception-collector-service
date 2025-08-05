package com.arcone.biopro.exceptioncollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class InterfaceExceptionCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterfaceExceptionCollectorApplication.class, args);
    }
}
