package com.arcone.biopro.distribution.shippingservice.verification.support;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class ApiHelper {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${api.base.url}")
    private String baseUrl;

    @Value("${api.graphql.url}")
    private String graphQlUrl;

    private WebClient webTestClientGraphQl;

    /**
     * This method is used to send a GET request to a specified endpoint and return the response.
     * It first checks if a custom base URL is provided. If not, it uses the default base URL.
     * It then constructs the full URI by appending the endpoint to the base URL.
     * It sends a GET request to the URI using the WebTestClient, and expects a 200 OK status.
     * The response body is expected to be a string.
     * It logs the URI and the response body, then returns the response.
     *
     * @param endpoint      The endpoint to which the GET request will be sent.
     * @param customBaseUrl The custom base URL to be used instead of the default one. If null, the default base URL is used.
     * @return An EntityExchangeResult object containing the response.
     */
    public EntityExchangeResult<String> getRequest(String endpoint, String customBaseUrl) {
        String url = customBaseUrl == null ? baseUrl : customBaseUrl;
        String uri = url + endpoint;


        var response = webTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult();
        log.info("GET request to {} returned: {}", uri, response.getResponseBody());
        return response;
    }

    /**
     * This method is a convenience method that sends a GET request to a specified endpoint using the default base URL.
     * It simply calls the getRequest method with the endpoint and null as the custom base URL.
     *
     * @param endpoint The endpoint to which the GET request will be sent.
     * @return An EntityExchangeResult object containing the response.
     */
    public EntityExchangeResult<String> getRequest(String endpoint) {
        return getRequest(endpoint, null);
    }

    @PostConstruct
    public void setupWebClient() {
        webTestClient = webTestClient.mutate()
            .responseTimeout(Duration.ofMillis(30000))
            .build();

        this.webTestClientGraphQl = WebClient.builder().baseUrl(graphQlUrl).build();
    }

    public Map graphQlRequest(String document, String path) {
        HttpGraphQlClient qlClient = HttpGraphQlClient.create(webTestClientGraphQl);

        return qlClient.document(document).retrieveSync(path).toEntity(Map.class);
    }
}
