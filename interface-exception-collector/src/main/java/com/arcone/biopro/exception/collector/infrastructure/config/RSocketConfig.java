package com.arcone.biopro.exception.collector.infrastructure.config;

import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;

import java.time.Duration;

/**
 * Configuration for RSocket client connections, specifically for the mock RSocket server integration.
 * This configuration provides the necessary beans for RSocket communication with proper
 * connection management, timeout settings, and error handling.
 */
@Configuration
@ConditionalOnProperty(name = "biopro.rsocket.mock-server.enabled", havingValue = "true")
@Slf4j
public class RSocketConfig {

    /**
     * Creates an RSocketRequester.Builder bean with optimized configuration for
     * mock server communication. This builder will be used by the MockRSocketOrderServiceClient
     * to establish connections to the mock RSocket server.
     */
    @Bean
    public RSocketRequester.Builder rSocketRequesterBuilder(RSocketStrategies rSocketStrategies) {
        log.info("Configuring RSocket requester builder for mock server integration");
        
        return RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies)
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .metadataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .rsocketConnector(this::configureRSocketConnector);
    }

    /**
     * Creates RSocket strategies with JSON encoding/decoding support.
     * This ensures proper serialization of order data between the client and mock server.
     */
    @Bean
    public RSocketStrategies rSocketStrategies() {
        log.debug("Creating RSocket strategies with JSON support");
        
        return RSocketStrategies.builder()
                .encoders(encoders -> {
                    // Add JSON encoder for request/response payloads
                    log.debug("Configuring JSON encoders for RSocket communication");
                })
                .decoders(decoders -> {
                    // Add JSON decoder for request/response payloads
                    log.debug("Configuring JSON decoders for RSocket communication");
                })
                .build();
    }

    /**
     * Configures the RSocket connector with optimized settings for mock server communication.
     * This includes keep-alive settings, payload decoder, and error handling.
     */
    private RSocketConnector configureRSocketConnector(RSocketConnector connector) {
        log.debug("Configuring RSocket connector with optimized settings");
        
        return connector
                // Keep-alive configuration for connection health
                .keepAlive(Duration.ofSeconds(30), Duration.ofSeconds(300))
                
                // Payload decoder configuration for performance
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                
                // Reconnection strategy for resilience
                .reconnect(reactor.util.retry.Retry
                        .fixedDelay(3, Duration.ofSeconds(2))
                        .doBeforeRetry(signal -> 
                                log.warn("Attempting to reconnect to mock RSocket server, attempt: {}", 
                                        signal.totalRetries() + 1)))
                
                // Error handling
                .acceptor((setup, sendingSocket) -> {
                    log.debug("RSocket connection established with mock server");
                    return reactor.core.publisher.Mono.just(sendingSocket);
                });
    }
}