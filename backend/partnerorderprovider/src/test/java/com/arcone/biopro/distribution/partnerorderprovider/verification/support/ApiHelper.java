package com.arcone.biopro.distribution.partnerorderprovider.verification.support;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.*;

import java.time.Duration;

@Component
@Slf4j
public class ApiHelper {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${api.base.url}")
    private String baseUrl;

    private RestTemplate restTemplate;

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

    /**
     * This method is used to send a POST request to a specified endpoint with a given body and return the response.
     * It first checks if a custom base URL is provided. If not, it uses the default base URL.
     * It then constructs the full URI by appending the endpoint to the base URL.
     * It sends a POST request to the URI using the WebTestClient, with the given body.
     * The response body is expected to be a string.
     * It logs the URI, the request body, and the response body, then returns the response.
     *
     * @param endpoint      The endpoint to which the POST request will be sent.
     * @param body          The body of the POST request.
     * @param customBaseUrl The custom base URL to be used instead of the default one. If null, the default base URL is used.
     * @return An EntityExchangeResult object containing the response.
     */
    public ResponseEntity<String> postRequest(String endpoint, String body, String customBaseUrl) {
        String url = (customBaseUrl == null ? baseUrl : customBaseUrl) + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            // Attempt to send the request
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("POST request to {} with body {} returned: {}", url, body, response.getBody());
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle client and server errors
            log.error("Request to {} failed with status code {}: {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            // Handle I/O errors
            log.error("Request to {} failed: {}", url, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to access resource");
        }
    }

    /**
     * This method is a convenience method that sends a POST request to a specified endpoint with a given body using the default base URL.
     * It simply calls the postRequest method with the endpoint, body, and null as the custom base URL.
     *
     * @param endpoint The endpoint to which the POST request will be sent.
     * @param body     The body of the POST request.
     * @return An EntityExchangeResult object containing the response.
     */
    public ResponseEntity<String> postRequest(String endpoint, String body) {
        return postRequest(endpoint, body, null);
    }

    @PostConstruct
    public void setupWebClient() {
        webTestClient = webTestClient.mutate()
            .responseTimeout(Duration.ofMillis(30000))
            .build();
        restTemplate = new RestTemplate();
    }

}
