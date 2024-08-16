package com.arcone.biopro.distribution.order.infrastructure.config;


import io.rsocket.frame.decoder.PayloadDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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



    @Bean
    @Qualifier("customer")
    public RSocketRequester getCustomerRSocketRequester(RSocketStrategies rSocketStrategies , @Value("${async-call.customer.tcp-address}") String rsocketServerHost
        , @Value("${async-call.customer.tcp-port}") Integer rsocketServerPort , @Value("${async-call.customer.max-attempts}") Integer maxAttempts
        , @Value("${async-call.customer.duration}")Integer duration){

        return buildRSocketRequester(rSocketStrategies, rsocketServerHost, rsocketServerPort, maxAttempts, duration);
    }

    @Bean
    @Qualifier("inventory")
    public RSocketRequester getInventoryRSocketRequester(RSocketStrategies rSocketStrategies , @Value("${async-call.inventory.tcp-address}") String rsocketServerHost
        , @Value("${async-call.inventory.tcp-port}") Integer rsocketServerPort , @Value("${async-call.inventory.max-attempts}") Integer maxAttempts
        , @Value("${async-call.inventory.duration}")Integer duration){

        return buildRSocketRequester(rSocketStrategies, rsocketServerHost, rsocketServerPort, maxAttempts, duration);
    }

    private RSocketRequester buildRSocketRequester( RSocketStrategies rSocketStrategies , String rsocketServerHost
        ,  Integer rsocketServerPort ,  Integer maxAttempts , Integer duration){
        return RSocketRequester.builder()
            .rsocketConnector(
                connector -> connector
                    .payloadDecoder(PayloadDecoder.ZERO_COPY)
                    .dataMimeType(MimeTypeUtils.APPLICATION_JSON_VALUE)
                    .reconnect(Retry.fixedDelay(maxAttempts, Duration.ofSeconds(duration)))
            )
            .rsocketStrategies(rSocketStrategies)
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .tcp(rsocketServerHost, rsocketServerPort);
    }

}
