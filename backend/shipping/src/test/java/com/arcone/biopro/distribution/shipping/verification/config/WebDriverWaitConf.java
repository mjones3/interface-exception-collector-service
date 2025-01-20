package com.arcone.biopro.distribution.shipping.verification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.MicrometerProducerListener;
import reactor.kafka.sender.SenderOptions;

import java.time.Duration;

@Lazy
@Configuration
public class WebDriverWaitConf {

    @Value("${default.testing.timeout:15}")
    private int timeout;

    @Bean
    @Lazy
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WebDriverWait webDriverWait(WebDriver driver){
        return new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    @EnableKafka
    @Configuration
    @RequiredArgsConstructor
    @Slf4j
    public static class KafkaTestConfiguration {
        @Bean
        ReactiveKafkaProducerTemplate<String, Object> producerTemplateOrder(KafkaProperties kafkaProperties, ObjectMapper objectMapper , MeterRegistry meterRegistry) {
            var props = kafkaProperties.buildProducerProperties(null);
            props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());

            return new ReactiveKafkaProducerTemplate<>(SenderOptions.<String, Object>create(props).withValueSerializer(new JsonSerializer<>(objectMapper)).maxInFlight(1)
                .producerListener(new MicrometerProducerListener(meterRegistry))
            );
        }
    }
}
