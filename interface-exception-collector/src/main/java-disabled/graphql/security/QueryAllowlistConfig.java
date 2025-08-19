package com.arcone.biopro.exception.collector.api.graphql.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for GraphQL query allowlist in production environments.
 * Maintains a list of approved query hashes that can be executed.
 */
@Configuration
@ConfigurationProperties(prefix = "graphql.security.query-allowlist")
@Slf4j
public class QueryAllowlistConfig {

    private boolean enabled = false;
    private Set<String> allowedQueryHashes = new HashSet<>();

    public QueryAllowlistConfig() {
        initializeDefaultAllowedQueries();
    }

    /**
     * Initializes default allowed queries for common dashboard operations.
     * In production, these would be populated from a secure configuration source.
     */
    private void initializeDefaultAllowedQueries() {
        // Example hashes for common queries - these would be generated from actual
        // queries
        allowedQueryHashes.add("a1b2c3d4e5f6..."); // exceptions query
        allowedQueryHashes.add("f6e5d4c3b2a1..."); // exception by ID query
        allowedQueryHashes.add("1a2b3c4d5e6f..."); // exception summary query
        allowedQueryHashes.add("6f5e4d3c2b1a..."); // retry exception mutation
        allowedQueryHashes.add("b1c2d3e4f5a6..."); // acknowledge exception mutation

        // Introspection queries (disabled in production by default)
        // allowedQueryHashes.add("introspection_hash...");

        log.info("Initialized query allowlist with {} approved queries", allowedQueryHashes.size());
    }

    /**
     * Adds a query hash to the allowlist.
     */
    public void addAllowedQuery(String queryHash) {
        allowedQueryHashes.add(queryHash);
        log.info("Added query hash to allowlist: {}", queryHash);
    }

    /**
     * Removes a query hash from the allowlist.
     */
    public void removeAllowedQuery(String queryHash) {
        boolean removed = allowedQueryHashes.remove(queryHash);
        if (removed) {
            log.info("Removed query hash from allowlist: {}", queryHash);
        }
    }

    /**
     * Checks if a query hash is allowed.
     */
    public boolean isQueryAllowed(String queryHash) {
        return allowedQueryHashes.contains(queryHash);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("Query allowlist enabled: {}", enabled);
    }

    public Set<String> getAllowedQueryHashes() {
        return new HashSet<>(allowedQueryHashes);
    }

    public void setAllowedQueryHashes(Set<String> allowedQueryHashes) {
        this.allowedQueryHashes = new HashSet<>(allowedQueryHashes);
        log.info("Updated allowlist with {} query hashes", allowedQueryHashes.size());
    }
}