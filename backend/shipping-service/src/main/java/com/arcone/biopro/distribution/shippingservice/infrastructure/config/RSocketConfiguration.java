package com.arcone.biopro.distribution.shippingservice.infrastructure.config;

import io.rsocket.frame.decoder.PayloadDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@Slf4j
public class RSocketConfiguration {

    @Value("${async-call.inventory.tcp-address}")
    private String rsocketServerHost;

    @Value("${async-call.inventory.tcp-port}")
    private Integer rsocketServerPort;

    @Value("${async-call.inventory.max-attempts}")
    private Integer maxAttempts;

    @Value("${async-call.inventory.duration}")
    private Integer duration;

    @Bean
    public RSocketRequester getRSocketRequester(RSocketStrategies rSocketStrategies){
        RSocketRequester.Builder builder = RSocketRequester.builder();
        return builder
            .rsocketConnector(
                rSocketConnector ->
                    rSocketConnector
                        .payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .dataMimeType(MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .reconnect(Retry.fixedDelay(maxAttempts, Duration.ofSeconds(duration)))
            )
            .rsocketStrategies(rSocketStrategies)
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .tcp(rsocketServerHost, rsocketServerPort);
    }
}
