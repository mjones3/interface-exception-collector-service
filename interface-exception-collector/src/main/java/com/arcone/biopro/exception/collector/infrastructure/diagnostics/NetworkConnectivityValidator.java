package com.arcone.biopro.exception.collector.infrastructure.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive network connectivity validator for testing connectivity
 * between Interface Exception Collector and Partner Order Service.
 */
@Component
public class NetworkConnectivityValidator {

    private static final Logger logger = LoggerFactory.getLogger(NetworkConnectivityValidator.class);
    private static final int DEFAULT_TIMEOUT_MS = 5000;

    /**
     * Performs comprehensive connectivity tests to the specified host and port.
     * 
     * @param host target host
     * @param port target port
     * @param timeoutMs connection timeout in milliseconds
     * @return list of test results
     */
    public List<NetworkTestResult> performComprehensiveConnectivityTest(String host, int port, int timeoutMs) {
        List<NetworkTestResult> results = new ArrayList<>();
        
        logger.debug("Starting comprehensive connectivity test for {}:{}", host, port);
        
        // Test 1: DNS Resolution
        results.add(testDnsResolution(host));
        
        // Test 2: Basic TCP Connectivity
        results.add(testTcpConnectivity(host, port, timeoutMs));
        
        // Test 3: Port Reachability
        results.add(testPortReachability(host, port, timeoutMs));
        
        // Test 4: Network Route Test
        results.add(testNetworkRoute(host));
        
        logger.debug("Completed comprehensive connectivity test for {}:{} with {} results", 
                    host, port, results.size());
        
        return results;
    }

    /**
     * Tests DNS resolution for the specified host.
     * 
     * @param host target host
     * @return DNS resolution test result
     */
    public NetworkTestResult testDnsResolution(String host) {
        long startTime = System.currentTimeMillis();
        
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> details = new HashMap<>();
            List<String> resolvedAddresses = new ArrayList<>();
            for (InetAddress addr : addresses) {
                resolvedAddresses.add(addr.getHostAddress());
            }
            details.put("resolvedAddresses", resolvedAddresses);
            details.put("addressCount", addresses.length);
            
            String summary = String.format("DNS resolution successful for %s (%d addresses)", 
                                         host, addresses.length);
            
            return NetworkTestResult.success("DNS_RESOLUTION", summary, duration, details);
            
        } catch (UnknownHostException e) {
            long duration = System.currentTimeMillis() - startTime;
            String summary = String.format("DNS resolution failed for %s", host);
            return NetworkTestResult.failure("DNS_RESOLUTION", summary, duration, e);
        }
    }

    /**
     * Tests basic TCP connectivity to the specified host and port.
     * 
     * @param host target host
     * @param port target port
     * @param timeoutMs connection timeout
     * @return TCP connectivity test result
     */
    public NetworkTestResult testTcpConnectivity(String host, int port, int timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> details = new HashMap<>();
            details.put("localAddress", socket.getLocalAddress().getHostAddress());
            details.put("localPort", socket.getLocalPort());
            details.put("remoteAddress", socket.getRemoteSocketAddress().toString());
            details.put("connected", socket.isConnected());
            
            String summary = String.format("TCP connection successful to %s:%d", host, port);
            
            return NetworkTestResult.success("TCP_CONNECTIVITY", summary, duration, details);
            
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            String summary = String.format("TCP connection failed to %s:%d", host, port);
            return NetworkTestResult.failure("TCP_CONNECTIVITY", summary, duration, e);
        }
    }

    /**
     * Tests port reachability using InetAddress.isReachable().
     * 
     * @param host target host
     * @param port target port (for logging purposes)
     * @param timeoutMs reachability timeout
     * @return port reachability test result
     */
    public NetworkTestResult testPortReachability(String host, int port, int timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        try {
            InetAddress address = InetAddress.getByName(host);
            boolean reachable = address.isReachable(timeoutMs);
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> details = new HashMap<>();
            details.put("hostAddress", address.getHostAddress());
            details.put("canonicalHostName", address.getCanonicalHostName());
            details.put("reachable", reachable);
            
            if (reachable) {
                String summary = String.format("Host %s is reachable", host);
                return NetworkTestResult.success("PORT_REACHABILITY", summary, duration, details);
            } else {
                String summary = String.format("Host %s is not reachable", host);
                return NetworkTestResult.failure("PORT_REACHABILITY", summary, duration, 
                    new IOException("Host not reachable"), details);
            }
            
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            String summary = String.format("Reachability test failed for %s", host);
            return NetworkTestResult.failure("PORT_REACHABILITY", summary, duration, e);
        }
    }

    /**
     * Tests network route to the specified host.
     * This is a basic implementation that checks if we can resolve and reach the host.
     * 
     * @param host target host
     * @return network route test result
     */
    public NetworkTestResult testNetworkRoute(String host) {
        long startTime = System.currentTimeMillis();
        
        try {
            InetAddress address = InetAddress.getByName(host);
            
            // Check if it's a local address
            boolean isLocal = address.isLoopbackAddress() || address.isSiteLocalAddress();
            
            Map<String, Object> details = new HashMap<>();
            details.put("hostAddress", address.getHostAddress());
            details.put("isLoopback", address.isLoopbackAddress());
            details.put("isSiteLocal", address.isSiteLocalAddress());
            details.put("isMulticast", address.isMulticastAddress());
            details.put("isLocal", isLocal);
            
            long duration = System.currentTimeMillis() - startTime;
            String summary = String.format("Network route analysis completed for %s (%s)", 
                                         host, isLocal ? "local" : "remote");
            
            return NetworkTestResult.success("NETWORK_ROUTE", summary, duration, details);
            
        } catch (UnknownHostException e) {
            long duration = System.currentTimeMillis() - startTime;
            String summary = String.format("Network route analysis failed for %s", host);
            return NetworkTestResult.failure("NETWORK_ROUTE", summary, duration, e);
        }
    }

    /**
     * Performs a quick connectivity check (TCP only).
     * 
     * @param host target host
     * @param port target port
     * @return true if TCP connection succeeds, false otherwise
     */
    public boolean quickConnectivityCheck(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), DEFAULT_TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            logger.debug("Quick connectivity check failed for {}:{}: {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * Performs asynchronous connectivity validation.
     * 
     * @param host target host
     * @param port target port
     * @param timeoutMs connection timeout
     * @return CompletableFuture containing test results
     */
    public CompletableFuture<List<NetworkTestResult>> performAsyncConnectivityTest(String host, int port, int timeoutMs) {
        return CompletableFuture.supplyAsync(() -> 
            performComprehensiveConnectivityTest(host, port, timeoutMs))
            .orTimeout(timeoutMs + 5000, TimeUnit.MILLISECONDS)
            .exceptionally(throwable -> {
                logger.warn("Async connectivity test failed for {}:{}: {}", host, port, throwable.getMessage());
                return Collections.singletonList(
                    NetworkTestResult.failure("ASYNC_TEST", 
                        "Async connectivity test failed", 
                        timeoutMs, 
                        new RuntimeException(throwable))
                );
            });
    }

    /**
     * Validates connectivity with retry logic.
     * 
     * @param host target host
     * @param port target port
     * @param timeoutMs connection timeout
     * @param maxRetries maximum retry attempts
     * @return true if any retry succeeds, false if all fail
     */
    public boolean validateConnectivityWithRetry(String host, int port, int timeoutMs, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            logger.debug("Connectivity validation attempt {}/{} for {}:{}", attempt, maxRetries, host, port);
            
            if (quickConnectivityCheck(host, port)) {
                logger.debug("Connectivity validation succeeded on attempt {}", attempt);
                return true;
            }
            
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000); // 1 second delay between retries
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Connectivity validation interrupted");
                    return false;
                }
            }
        }
        
        logger.warn("Connectivity validation failed after {} attempts", maxRetries);
        return false;
    }

    /**
     * Gets a summary of connectivity test results.
     * 
     * @param results list of test results
     * @return formatted summary string
     */
    public String getConnectivitySummary(List<NetworkTestResult> results) {
        if (results == null || results.isEmpty()) {
            return "No connectivity tests performed";
        }
        
        long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        long totalTests = results.size();
        
        return String.format("Connectivity Summary: %d/%d tests passed", successCount, totalTests);
    }
}