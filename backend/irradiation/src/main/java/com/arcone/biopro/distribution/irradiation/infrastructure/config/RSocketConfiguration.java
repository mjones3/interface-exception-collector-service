package com.arcone.biopro.distribution.irradiation.infrastructure.config;

import io.rsocket.core.Resume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@Slf4j
class RSocketConfiguration {


    /**
     * Add resume ability for RSocketServer
     *
     * @return RSocketServerCustomizer
     */
    @Bean
    RSocketServerCustomizer resumeServerCustomizer() {
        return rSocketServer ->
            rSocketServer.resume(new Resume()
                .streamTimeout(Duration.ofSeconds(60))
                .sessionDuration(Duration.ofMinutes(5))
                .retry(
                    Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(s -> log.warn("Client disconnected. Trying to resume connection..."))
                )
            );
    }

}
