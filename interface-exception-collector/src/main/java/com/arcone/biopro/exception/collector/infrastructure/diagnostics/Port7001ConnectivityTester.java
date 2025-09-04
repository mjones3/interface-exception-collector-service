package com.arcone.biopro.exception.collector.infrastructure.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Specialized connectivity tester for port 7001 (Partner Order Service RSocket port).
 * Provides targeted connectivity testing and diagnostics for the Partner Order Service.
 */
@Component
public class Port7001ConnectivityTester {

    private static final Logger logger = LoggerFactory.getLogger(Port7001ConnectivityTester.class);
    private static final int DEFAULT_TIMEOUT_MS = 5000; // 5 seconds
    private static final int QUICK_TIMEOUT_MS = 2000; // 2 seconds for quick checks

    /**
     * Tests connectivity to port 7001 on the specified host.
     * 
     * @param host the target host
     * @return true if port 7001 is reachable, false otherwise
     */
    public boolean testPort7001Connectivity(String host) {
        return testPort7001Connectivity(host, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Tests connectivity to port 7001 on the specified host with custom timeout.
     * 
     * @param host the target host
     * @param timeoutMs connection timeout in milliseconds
     * @return true if port 7001 is reachable, false otherwise
     */
    public boolean testPort7001Connectivity(String host, int timeoutMs) {
        logger.debug("Testing connectivity to {}:7001 with timeout {}ms", host, timeoutMs);
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 7001), timeoutMs);
            logger.debug("✅ Successfully connected to {}:7001", host);
            return true;
            
        } catch (SocketTimeoutException e) {
            logger.warn("❌ Connection to {}:7001 timed out after {}ms", host, timeoutMs);
            return false;
            
        } catch (IOException e) {
            logger.warn("❌ Failed to connect to {}:7001: {}", host, e.getMessage());
            return false;
            
        } catch (Exception e) {
            logger.error("❌ Unexpected error testing connectivity to {}:7001", host, e);
            return false;
        }
    }

    /**
     * Performs a quick connectivity test to port 7001.
     * Uses a shorter timeout for rapid health checks.
     * 
     * @param host the target host
     * @return true if port 7001 is quickly reachable, false otherwise
     */
    public boolean quickPort7001Test(String host) {
        return testPort7001Connectivity(host, QUICK_TIMEOUT_MS);
    }

    /**
     * Performs an asynchronous connectivity test to port 7001.
     * 
     * @param host the target host
     * @return CompletableFuture containing the connectivity test result
     */
    public CompletableFuture<Boolean> asyncPort7001Test(String host) {
        return CompletableFuture.supplyAsync(() -> testPort7001Connectivity(host))
                .orTimeout(DEFAULT_TIMEOUT_MS + 1000, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    logger.warn("Async port 7001 test failed for host {}: {}", host, throwable.getMessage());
                    return false;
                });
    }

    /**
     * Tests connectivity to multiple hosts on port 7001.
     * 
     * @param hosts array of hosts to test
     * @return true if all hosts are reachable on port 7001, false otherwise
     */
    public boolean testMultipleHosts(String... hosts) {
        if (hosts == null || hosts.length == 0) {
            logger.warn("No hosts provided for port 7001 connectivity test");
            return false;
        }

        logger.info("Testing port 7001 connectivity for {} hosts", hosts.length);
        
        boolean allSuccessful = true;
        for (String host : hosts) {
            boolean result = testPort7001Connectivity(host);
            if (!result) {
                allSuccessful = false;
            }
            logger.debug("Host {} port 7001 test: {}", host, result ? "PASS" : "FAIL");
        }
        
        logger.info("Port 7001 connectivity test summary: {}/{} hosts reachable", 
                   countSuccessfulTests(hosts), hosts.length);
        
        return allSuccessful;
    }

    /**
     * Counts how many hosts are successfully reachable on port 7001.
     * 
     * @param hosts array of hosts to test
     * @return number of successfully reachable hosts
     */
    public int countSuccessfulTests(String... hosts) {
        if (hosts == null || hosts.length == 0) {
            return 0;
        }

        int successCount = 0;
        for (String host : hosts) {
            if (quickPort7001Test(host)) {
                successCount++;
            }
        }
        
        return successCount;
    }

    /**
     * Provides detailed connectivity information for port 7001.
     * 
     * @param host the target host
     * @return formatted string with connectivity details
     */
    public String getPort7001ConnectivityDetails(String host) {
        long startTime = System.currentTimeMillis();
        boolean isReachable = testPort7001Connectivity(host);
        long duration = System.currentTimeMillis() - startTime;
        
        return String.format("Host: %s, Port: 7001, Reachable: %s, Test Duration: %dms", 
                           host, isReachable ? "YES" : "NO", duration);
    }

    /**
     * Validates that the Partner Order Service is running on the expected port 7001.
     * This is a convenience method for the most common use case.
     * 
     * @param partnerOrderServiceHost the Partner Order Service host
     * @return true if Partner Order Service is reachable on port 7001
     */
    public boolean validatePartnerOrderServicePort(String partnerOrderServiceHost) {
        logger.info("Validating Partner Order Service connectivity on port 7001");
        
        boolean result = testPort7001Connectivity(partnerOrderServiceHost);
        
        if (result) {
            logger.info("✅ Partner Order Service is reachable on {}:7001", partnerOrderServiceHost);
        } else {
            logger.warn("❌ Partner Order Service is NOT reachable on {}:7001", partnerOrderServiceHost);
            logger.warn("Please verify that:");
            logger.warn("  1. Partner Order Service is running");
            logger.warn("  2. Port 7001 is not blocked by firewall");
            logger.warn("  3. Network connectivity exists between services");
        }
        
        return result;
    }
}