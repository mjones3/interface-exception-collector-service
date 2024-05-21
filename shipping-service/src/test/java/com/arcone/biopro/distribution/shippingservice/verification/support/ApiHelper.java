package com.arcone.biopro.distribution.shippingservice.verification.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@Component
public class ApiHelper {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${base.url}")
    private String baseUrl;

    public EntityExchangeResult<String> getRequest(String endpoint, String customBaseUrl) throws InterruptedException {
        String url = customBaseUrl == null ? baseUrl : customBaseUrl;
        String uri = url + endpoint;
        return webTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult();
    }
}
