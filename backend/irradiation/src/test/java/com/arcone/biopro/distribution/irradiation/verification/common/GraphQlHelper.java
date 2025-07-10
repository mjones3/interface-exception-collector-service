package com.arcone.biopro.distribution.irradiation.verification.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GraphQlHelper {

    private final HttpGraphQlTester graphQlTester;
    private static final String DEFAULT_BASE_URL = "/irradiation/graphql";

    public GraphQlHelper(@Autowired WebTestClient webTestClient, @Value("${web-test-client.timeout-in-minutes}") Integer webTestClientTimeoutInMinutes) {
        this.graphQlTester = HttpGraphQlTester.create(webTestClient.mutate().responseTimeout(Duration.ofMinutes(webTestClientTimeoutInMinutes)).baseUrl(DEFAULT_BASE_URL).build());
    }

    public <T> GraphQlResponse<T> executeQuery(String documentName, Map<String, Object> variables, String path, Class<T> responseType) {
        var tester = graphQlTester.documentName(documentName);
        variables.forEach(tester::variable);
        List<ResponseError> errorsList = new ArrayList<>();
        var response = tester.execute()
            .errors()
            .satisfy(errorsList::addAll);
        T data = null;
        if (errorsList.isEmpty()) {
            data = response.path(path).entity(responseType).get();
        }
        return new GraphQlResponse<>(data, errorsList);
    }
}
