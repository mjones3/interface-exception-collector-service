package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * GraphQL error representation for structured error responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLError {

    private String message;
    private String code;
    private String path;
    private Map<String, Object> extensions;
}