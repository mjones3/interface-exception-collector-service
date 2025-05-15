package com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UnsuitableMessage.class, name = "ProductUnsuitable"),
    @JsonSubTypes.Type(value = UnsuitableMessage.class, name = "UnitUnsuitable")
})
public record UnsuitableMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Key identifying the reason for unsuitability")
    String reasonKey
) {
}

