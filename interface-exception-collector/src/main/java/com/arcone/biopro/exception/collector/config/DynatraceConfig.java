package com.arcone.biopro.exception.collector.config;

import com.dynatrace.oneagent.sdk.OneAgentSDK;
import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.dynatrace.DynatraceConfig;
import io.micrometer.dynatrace.DynatraceMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Configuration for Dynatrace monitoring and observability.
 * Provides comprehensive instrumentation for both performance metrics
 * and business-specific metrics related to interface exceptions.
 */
@Configuration
@Slf4j
public class DynatraceConfig {

    @Value("${dynatrace.enabled:true}")
    private boolean dynatraceEnabled;

    @Value("${dynatrace.api-token:}")
    private String apiToken;

    @Value("${dynatrace.uri:}")
    private String uri;

    @Value("${dynatrace.device-id:}")
    private String deviceId;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${dynatrace.step:PT1M}")
    private Duration step;

    @Value("${dynatrace.connect-timeout:PT10S}")
    private Duration connectTimeout;

    @Value("${dynatrace.read-timeout:PT10S}")
    private Duration readTimeout;

    /**
     * Creates and configures the Dynatrace OneAgent SDK.
     * This provides deep code-level insights and automatic instrumentation.
     *
     * @return configured OneAgent SDK instance
     */
    @Bean
    public OneAgentSDK oneAgentSDK() {
        if (!dynatraceEnabled) {
            log.info("Dynatrace monitoring is disabled");
            return OneAgentSDKFactory.createInstance();
        }

        OneAgentSDK oneAgentSDK = OneAgentSDKFactory.createInstance();

        switch (oneAgentSDK.getCurrentState()) {
            case ACTIVE:
                log.info("Dynatrace OneAgent SDK is active and ready for monitoring");
                break;
            case PERMANENTLY_INACTIVE:
                log.warn("Dynatrace OneAgent SDK is permanently inactive - agent not installed or configured");
                break;
            case TEMPORARILY_INACTIVE:
                log.warn("Dynatrace OneAgent SDK is temporarily inactive - will retry connection");
                break;
            default:
                log.warn("Dynatrace OneAgent SDK state: {}", oneAgentSDK.getCurrentState());
        }

        return oneAgentSDK;
    }

    /**
     * Creates and configures the Dynatrace meter registry for custom metrics.
     * This enables sending custom business metrics to Dynatrace.
     *
     * @return configured Dynatrace meter registry
     */
    @Bean
    @Primary
    public DynatraceMeterRegistry dynatraceMeterRegistry() {
        if (!dynatraceEnabled || apiToken.isEmpty() || uri.isEmpty()) {
            log.warn("Dynatrace meter registry disabled - missing configuration (enabled: {}, token: {}, uri: {})",
                    dynatraceEnabled, !apiToken.isEmpty(), !uri.isEmpty());
            return null;
        }

        DynatraceConfig dynatraceConfig = new DynatraceConfig() {
            @Override
            public String get(String key) {
                return null; // Use default values
            }

            @Override
            public String apiToken() {
                return apiToken;
            }

            @Override
            public String uri() {
                return uri;
            }

            @Override
            public String deviceId() {
                return deviceId.isEmpty() ? applicationName : deviceId;
            }

            @Override
            public Duration step() {
                return step;
            }

            @Override
            public Duration connectTimeout() {
                return connectTimeout;
            }

            @Override
            public Duration readTimeout() {
                return readTimeout;
            }
        };

        DynatraceMeterRegistry registry = new DynatraceMeterRegistry(dynatraceConfig);

        log.info("Dynatrace meter registry configured - URI: {}, Device ID: {}, Step: {}",
                uri, dynatraceConfig.deviceId(), step);

        return registry;
    }

    /**
     * Customizes the meter registry with common tags for better metric
     * organization.
     * Adds service identification and environment information to all metrics.
     *
     * @return meter registry customizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> dynatraceMeterRegistryCustomizer() {
        return registry -> {
            registry.config()
                    .commonTags(
                            "service", "interface-exception-collector",
                            "application", applicationName,
                            "component", "exception-management",
                            "domain", "biopro-interfaces");

            log.debug("Applied common tags to Dynatrace meter registry");
        };
    }
}