package com.arcone.biopro.distribution.inventory.domain.model;

import lombok.*;

/**
 * Domain model representing an inventory property.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"value"})
public class Property {
    private String key;
    private String value;
}
