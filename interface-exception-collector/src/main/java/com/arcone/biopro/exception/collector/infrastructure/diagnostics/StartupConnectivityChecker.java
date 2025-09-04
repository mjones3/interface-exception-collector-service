package com.arcone.biopro.exception.collector.infrastructure.diagnostics;

import com.arcone.biopro.exception.collector.infrastructure.config.RSocketProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Performs connectivity checks during application startup to validate
 * network connectivity between Interface Exception Collector and Partner Order Service.
 */
@Component
public class StartupConnectivityChecker {

    private static final Logger logger = LoggerFactory.getLogger(StartupConnectivityChecker.class);
    private static final int STARTUP_CHECK_TIMEOUT_MS = 10000; // 10 seconds
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 2000; // 2 seconds

    private final NetworkConnectivityValidator networkValidator;
    private final RSocketProperties rSocketProperties;

    @Autowired
    public StartupConnectivityChecker(NetworkConnectivityValidator networkValidator,
                                    RSocketProperties rSocketProperties) {
        this.networkValidator = networkValidator;
        this.rSocketProperties = rSocketProperties;
    }

    /**
     * Performs connectivity checks when the application is ready.
     * This ensures that network connectivity is validated before the application
     * starts processing requests.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void performStartupConnectivityChecks() {
        logger.info("Starting connectivity checks during application startup");
        
        try {
            // Get RSocket client configuration
            String partnerOrderHost = rSocketProperties.getPartnerOrderService().getHost();
            int partnerOrderPort = rSocketProperties.getPartnerOrderService().getPort();
            
            logger.info("Validating connectivity to Partner Order Service at {}:{}", 
                       partnerOrderHost, partnerOrderPort);
            
            // Perform connectivity checks with retries
            boolean connectivitySuccess = performConnectivityChecksWithRetry(
                partnerOrderHost, partnerOrderPort, RETRY_ATTEMPTS);
            
            if (connectivitySuccess) {
                logger.info("✅ Startup connectivity checks PASSED - Partner Order Service is reachable");
            } else {
                logger.warn("❌ Startup connectivity checks FAILED - Partner Order Service connectivity issues detected");
                logger.warn("Application will continue startup but RSocket communication may fail");
            }
            
        } catch (Exception e) {
            logger.error("Error during startup connectivity checks", e);
            logger.warn("Application will continue startup but connectivity issues may exist");
        }
    }

    /**
     * Performs connectivity checks with retry logic.
     * 
     * @param host the target host
     * @param port the target port
     * @param maxRetries maximum number of retry attempts
     * @return true if connectivity checks pass, false otherwise
     */
    private boolean performConnectivityChecksWithRetry(String host, int port, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            logger.debug("Connectivity check attempt {}/{} for {}:{}", attempt, maxRetries, host, port);
            
            try {
                List<NetworkTestResult> results = networkValidator.performComprehensiveConnectivityTest(
                    host, port, STARTUP_CHECK_TIMEOUT_MS);
                
                boolean allTestsPassed = results.stream().allMatch(NetworkTestResult::isSuccess);
                
                if (allTestsPassed) {
                    logger.info("Connectivity check attempt {}/{} PASSED", attempt, maxRetries);
                    logConnectivityResults(results, "PASSED");
                    return true;
                } else {
                    logger.warn("Connectivity check attempt {}/{} FAILED", attempt, maxRetries);
                    logConnectivityResults(results, "FAILED");
                    
                    if (attempt < maxRetries) {
                        logger.info("Retrying connectivity check in {}ms...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                }
                
            } catch (Exception e) {
                logger.warn("Connectivity check attempt {}/{} encountered error: {}", 
                           attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Connectivity check retry interrupted");
                        return false;
                    }
                }
            }
        }
        
        logger.error("All connectivity check attempts failed after {} retries", maxRetries);
        return false;
    }

    /**
     * Logs detailed connectivity test results.
     * 
     * @param results the test results to log
     * @param overallStatus the overall status (PASSED/FAILED)
     */
    private void logConnectivityResults(List<NetworkTestResult> results, String overallStatus) {
        logger.info("=== Connectivity Check Results ({}) ===", overallStatus);
        
        for (NetworkTestResult result : results) {
            if (result.isSuccess()) {
                logger.info("  ✅ {}", result.getSummary());
                
                // Log additional details for successful tests
                result.getDetail("resolvedAddresses").ifPresent(addresses -> 
                    logger.debug("    Resolved addresses: {}", addresses));
                result.getDetail("localAddress").ifPresent(addr -> 
                    logger.debug("    Local address: {}", addr));
                result.getDetail("remoteAddress").ifPresent(addr -> 
                    logger.debug("    Remote address: {}", addr));
                    
            } else {
                logger.warn("  ❌ {}", result.getSummary());
                
                // Log exception details for failed tests
                result.getException().ifPresent(ex -> 
                    logger.debug("    Exception details: {}", ex.getMessage()));
            }
        }
        
        logger.info("=== End Connectivity Check Results ===");
    }

    /**
     * Performs asynchronous connectivity validation.
     * This can be used for non-blocking connectivity checks.
     * 
     * @param host the target host
     * @param port the target port
     * @return CompletableFuture containing connectivity check result
     */
    public CompletableFuture<Boolean> performAsyncConnectivityCheck(String host, int port) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<NetworkTestResult> results = networkValidator.performComprehensiveConnectivityTest(
                    host, port, STARTUP_CHECK_TIMEOUT_MS);
                
                boolean allTestsPassed = results.stream().allMatch(NetworkTestResult::isSuccess);
                logConnectivityResults(results, allTestsPassed ? "PASSED" : "FAILED");
                
                return allTestsPassed;
                
            } catch (Exception e) {
                logger.error("Async connectivity check failed", e);
                return false;
            }
        });
    }

    /**
     * Validates connectivity to the configured Partner Order Service.
     * This method can be called programmatically to check connectivity.
     * 
     * @return true if connectivity is successful, false otherwise
     */
    public boolean validatePartnerOrderServiceConnectivity() {
        String host = rSocketProperties.getPartnerOrderService().getHost();
        int port = rSocketProperties.getPartnerOrderService().getPort();
        
        logger.info("Validating Partner Order Service connectivity to {}:{}", host, port);
        
        return performConnectivityChecksWithRetry(host, port, 1); // Single attempt for programmatic calls
    }

    /**
     * Gets the current RSocket client configuration for connectivity testing.
     * 
     * @return formatted string with host and port information
     */
    public String getRSocketClientConfiguration() {
        return String.format("%s:%d", 
                           rSocketProperties.getPartnerOrderService().getHost(), 
                           rSocketProperties.getPartnerOrderService().getPort());
    }
}