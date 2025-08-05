package com.arcone.biopro.distribution.irradiation.infrastructure.config;

import io.rsocket.core.Resume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@Slf4j
public class RSocketConfiguration {

    @Value("${spring.rsocket.inventory.host:inventory-service}")
    private String inventoryHost;

    @Value("${spring.rsocket.inventory.port:7002}")
    private int inventoryPort;

    @Value("${spring.rsocket.supply.host:supply-service}")
    private String supplyHost;

    @Value("${spring.rsocket.supply.port:7002}")
    private int supplyPort;

    @Bean
    RSocketServerCustomizer resumeServerCustomizer() {
        log.info("RSocketServerCustomizer for host {} and port {}", inventoryHost, inventoryPort);
        return rSocketServer ->
            rSocketServer.resume(new Resume()
                .streamTimeout(Duration.ofSeconds(5))
                .sessionDuration(Duration.ofSeconds(5))
                .retry(
                    Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(s -> log.warn("Client disconnected. Trying to resume connection..."))
                )
            );
    }

    @Bean
    public RSocketRequester inventoryRSocketRequester(RSocketStrategies strategies) {
        log.info("Creating RSocketRequester for host {} and port {}", inventoryHost, inventoryPort);
        return RSocketRequester.builder()
            .rsocketStrategies(strategies)
            .rsocketConnector(connector -> connector
                .reconnect(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                    .doBeforeRetry(s -> log.warn("Disconnected from inventory service. Trying to reconnect...")))
            )
            .tcp(inventoryHost, inventoryPort);
    }

    @Bean
    public RSocketRequester supplyRSocketRequester(RSocketStrategies strategies) {
        log.info("Creating RSocketRequester for supply service at host {} and port {}", supplyHost, supplyPort);
        return RSocketRequester.builder()
            .rsocketStrategies(strategies)
            .rsocketConnector(connector -> connector
                .reconnect(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                    .doBeforeRetry(s -> log.warn("Disconnected from supply service. Trying to reconnect...")))
            )
            .tcp(supplyHost, supplyPort);
    }
}
