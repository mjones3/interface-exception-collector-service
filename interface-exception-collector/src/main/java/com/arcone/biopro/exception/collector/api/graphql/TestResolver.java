package com.arcone.biopro.exception.collector.api.graphql;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Test GraphQL resolver to verify the GraphQL infrastructure is working
 * correctly.
 * This resolver provides basic health check endpoints for Query, Mutation, and
 * Subscription operations.
 */
@Controller
public class TestResolver {

    /**
     * Health check query to verify GraphQL Query operations are working.
     * 
     * @return A health status message with timestamp
     */
    @QueryMapping
    public String health() {
        return "GraphQL API is healthy at " + LocalDateTime.now();
    }

    /**
     * Ping mutation to verify GraphQL Mutation operations are working.
     * 
     * @return A pong response with timestamp
     */
    @MutationMapping
    public String ping() {
        return "Pong from GraphQL API at " + LocalDateTime.now();
    }

    /**
     * Heartbeat subscription to verify GraphQL Subscription operations are working.
     * Emits a heartbeat message every 5 seconds.
     * 
     * @return A Flux stream of heartbeat messages
     */
    @SubscriptionMapping
    public Flux<String> heartbeat() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(i -> "Heartbeat #" + i + " at " + LocalDateTime.now());
    }
}