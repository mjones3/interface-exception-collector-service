package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * TLS configuration for secure connections
 */
@Configuration
@Slf4j
public class TlsConfig {

    @Value("${app.security.tls.truststore.path:}")
    private String truststorePath;

    @Value("${app.security.tls.truststore.password:}")
    private String truststorePassword;

    @Value("${app.security.tls.keystore.path:}")
    private String keystorePath;

    @Value("${app.security.tls.keystore.password:}")
    private String keystorePassword;

    /**
     * SSL Context for secure connections when TLS is enabled
     */
    @Bean
    @ConditionalOnProperty(name = "app.security.tls.enabled", havingValue = "true")
    public SSLContext sslContext() {
        try {
            // Load truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            if (truststorePath != null && !truststorePath.isEmpty()) {
                try (FileInputStream trustStoreStream = new FileInputStream(truststorePath)) {
                    trustStore.load(trustStoreStream, truststorePassword.toCharArray());
                }
            } else {
                trustStore.load(null, null); // Use default truststore
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            log.info("SSL context initialized successfully");
            return sslContext;

        } catch (Exception e) {
            log.error("Failed to initialize SSL context", e);
            throw new RuntimeException("SSL context initialization failed", e);
        }
    }
}