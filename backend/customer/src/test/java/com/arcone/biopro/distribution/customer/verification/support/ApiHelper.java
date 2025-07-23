package com.arcone.biopro.distribution.customer.verification.support;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Component
@Slf4j
public class ApiHelper {

    @Autowired
    private SharedContext context;

    @Autowired
    private WebTestClient webTestClient;
    private WebClient webTestClientGraphQl;

    @Value("${api.base.url}")
    private String baseUrl;

    @Value("${api.graphql.url}")
    private String graphQlUrl;

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
            var response = qlClient.document(document).retrieveSync(path).toEntity(Map.class);
            // Set the API response to the context so that it can be used in other steps.
            var notifications = (ArrayList) response.get("notifications");
            if (notifications != null && !notifications.isEmpty()) {
                context.setApiMessageResponse((List<Map>) response.get("notifications"));
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

    public List<Map> graphQlListRequestWithErrorMessage(String document, String path) {
        HttpGraphQlClient qlClient = HttpGraphQlClient.create(webTestClientGraphQl);
        try {
            // Execute the request but don't immediately transform to specific path
            Map<String, Object> fullResponse = qlClient.document(document)
                .execute()
                .block()
                .toEntity(Map.class);

            // Check for GraphQL errors first
            if (fullResponse.containsKey("errors")) {
                // Return the error response directly
                return Collections.singletonList(fullResponse);
            }

            // If no errors, process the data path
            if (fullResponse.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) fullResponse.get("data");

                // Handle nested path data
                if (data.containsKey(path)) {
                    Object pathData = data.get(path);
                    if (pathData instanceof List) {
                        return (List<Map>) pathData;
                    }
                    return Collections.singletonList((Map) pathData);
                }

                // If path not found, return full data
                return Collections.singletonList(data);
            }

            // If neither errors nor data, return the full response
            return Collections.singletonList(fullResponse);

        } catch (Exception e) {
            log.error("Error executing GraphQL request: {}", e.getMessage());
            // Create error response structure
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errors", Collections.singletonList(
                Map.of("message", e.getMessage())
            ));
            return Collections.singletonList(errorResponse);
        }
    }

    private void setErrorContext(FieldAccessException e){
        log.error("Not able to retrieve data from {}", e.getResponse());
        var error = e.getResponse().getErrors().getFirst();
        var errorMap = new HashMap<String, Object>();
        errorMap.put("classification", error.getErrorType().toString());
        errorMap.put("message", error.getMessage());
        context.setApiErrorResponse(errorMap);
    }

    public Object[] graphQlRequestObjectList(String document, String path) {
        HttpGraphQlClient qlClient = HttpGraphQlClient.create(webTestClientGraphQl);
        return qlClient.document(document).retrieveSync(path).toEntity(Object[].class);
    }
}
