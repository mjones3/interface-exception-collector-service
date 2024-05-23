package com.arcone.biopro.distribution.shippingservice.verification.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@Component
@Slf4j
public class ApiHelper {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${base.url}")
    private String baseUrl;

    public EntityExchangeResult<String> getRequest(String endpoint, String customBaseUrl) {
        String url = customBaseUrl == null ? baseUrl : customBaseUrl;
        String uri = url + endpoint;
        var response =  webTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult();
        log.info("GET request to {} returned: {}", uri, response.getResponseBody());
        return response;
    }

    public EntityExchangeResult<String> getRequest(String endpoint) {
        return getRequest(endpoint, null);
    }
}
