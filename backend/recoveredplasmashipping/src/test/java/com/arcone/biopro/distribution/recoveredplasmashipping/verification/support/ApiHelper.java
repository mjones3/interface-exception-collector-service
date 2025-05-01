package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ApiHelper {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SharedContext context;

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
    public EntityExchangeResult<String> postRequest(String endpoint, String body, String customBaseUrl) {
        String url = customBaseUrl == null ? baseUrl : customBaseUrl;
        String uri = url + endpoint;

        var response = webTestClient.post()
            .uri(uri)
            .header("Content-Type", "application/json")
            .bodyValue(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult();
        log.info("POST request to {} with body {} returned: {}", uri, body, response.getResponseBody());
        return response;
    }

    /**
     * This method is a convenience method that sends a POST request to a specified endpoint with a given body using the default base URL.
     * It simply calls the postRequest method with the endpoint, body, and null as the custom base URL.
     *
     * @param endpoint The endpoint to which the POST request will be sent.
     * @param body     The body of the POST request.
     * @return An EntityExchangeResult object containing the response.
     */
    public EntityExchangeResult<String> postRequest(String endpoint, String body) {
        return postRequest(endpoint, body, null);
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
        try {
            log.debug("Path: {}", path);
            log.debug("Request: {}", document);
            var response = qlClient.document(document).retrieveSync(path).toEntity(Map.class);
            // Set the API response to the context so that it can be used in other steps.
            log.debug("Response: {}", response);
            var notifications = (ArrayList) response.get("notifications");
            if (notifications != null && !notifications.isEmpty()) {
                context.setApiListMessageResponse((List<Map>) response.get("notifications"));
            }
            return response;
        } catch (FieldAccessException e) {
            this.setErrorContext(e);
            return e.getResponse().toMap();
        }
    }

    public List<Map> graphQlListRequest(String document, String path) {
        HttpGraphQlClient qlClient = HttpGraphQlClient.create(webTestClientGraphQl);
        try {
            return qlClient.document(document).retrieveSync(path).toEntityList(Map.class);
        } catch (FieldAccessException e) {
            this.setErrorContext(e);
            return Collections.emptyList();
        }
    }

    private void setErrorContext(FieldAccessException e){
        log.error("Not able to retrieve data from {}", e.getResponse());
        var error = e.getResponse().getErrors().getFirst();
        var errorMap = new HashMap<>();
        errorMap.put("classification", error.getErrorType().toString());
        errorMap.put("message", error.getMessage());
        context.setApiErrorResponse(errorMap);
    }

    public Object[] graphQlRequestObjectList(String document, String path) {
        HttpGraphQlClient qlClient = HttpGraphQlClient.create(webTestClientGraphQl);
        return qlClient.document(document).retrieveSync(path).toEntity(Object[].class);
    }
}
