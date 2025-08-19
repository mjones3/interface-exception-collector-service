package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GraphQL connection type for paginated exception results.
 * Implements the Relay connection specification for cursor-based pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionConnection {

    private List<ExceptionEdge> edges;
    private PageInfo pageInfo;
    private Long totalCount;

    /**
     * Edge type containing the exception node and cursor.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionEdge {
        private InterfaceException node;
        private String cursor;
    }

    /**
     * Page information for pagination metadata.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private Boolean hasNextPage;
        private Boolean hasPreviousPage;
        private String startCursor;
        private String endCursor;
    }
}