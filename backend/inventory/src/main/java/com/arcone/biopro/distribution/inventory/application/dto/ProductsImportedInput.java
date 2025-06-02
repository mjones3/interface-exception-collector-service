package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ProductsImportedInput {
    List<ProductCreatedInput> products;
}
