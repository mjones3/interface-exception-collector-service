package com.arcone.biopro.exception.collector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate used for external service calls.
 */
@Configuration
public class RestTemplateConfig {

    @Value("${app.source-services.timeout:5000}")
    private int serviceTimeout;

    @Value("${app.source-services.connection-timeout:3000}")
    private int connectionTimeout;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setConnectionRequestTimeout(serviceTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);

        // Add error handler if needed
        // restTemplate.setErrorHandler(new CustomResponseErrorHandler());

        return restTemplate;
    }
}