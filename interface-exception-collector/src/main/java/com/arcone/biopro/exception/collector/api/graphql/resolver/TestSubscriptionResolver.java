package com.arcone.biopro.exception.collector.api.graphql.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Simple test resolver to verify subscription mapping works.
 */
@Controller
@Slf4j
public class TestSubscriptionResolver {

    @SubscriptionMapping("testSubscription")
    public Flux<String> testSubscription() {
        log.info("🧪 Test subscription called");
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Test message " + i)
                .take(5);
    }
}