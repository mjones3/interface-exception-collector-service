package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * GraphQL input type for filtering subscription events.
 * Maps to the SubscriptionFilters input type defined in the GraphQL schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionFilters {

    @Size(max = 10, message = "Cannot filter by more than 10 interface types")
    private List<InterfaceType> interfaceTypes;

    @Size(max = 10, message = "Cannot filter by more than 10 severities")
    private List<ExceptionSeverity> severities;

    @Size(max = 50, message = "Cannot filter by more than 50 customer IDs")
    private List<@Pattern(regexp = "^[A-Za-z0-9\\-_]{1,20}$", message = "Invalid customer ID format") String> customerIds;

    @Size(max = 50, message = "Cannot filter by more than 50 location codes")
    private List<@Pattern(regexp = "^[A-Za-z0-9\\-_]{1,10}$", message = "Invalid location code format") String> locationCodes;

    private Boolean includeResolved;

    /**
     * Converts SubscriptionFilters to ExceptionFilters for reuse of filtering
     * logic.
     */
    public ExceptionFilters toExceptionFilters() {
        return ExceptionFilters.builder()
                .interfaceTypes(this.interfaceTypes)
                .severities(this.severities)
                .customerIds(this.customerIds)
                .locationCodes(this.locationCodes)
                .excludeResolved(this.includeResolved != null ? !this.includeResolved : null)
                .build();
    }
}