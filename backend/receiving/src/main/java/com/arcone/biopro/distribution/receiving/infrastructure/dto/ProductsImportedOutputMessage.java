package com.arcone.biopro.distribution.receiving.infrastructure.dto;

import com.arcone.biopro.distribution.receiving.infrastructure.event.AbstractOutputEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ProductsImported",
    title = "ProductsImported",
    description = "Products Imported Event"
)
@Getter
public class ProductsImportedOutputMessage extends AbstractOutputEvent<ProductsImportedPayload> {

    private final static String eventVersion = "1.0";
    private final static String eventType = "ProductsImported";


    public ProductsImportedOutputMessage(ProductsImportedPayload payload) {
        super( UUID.randomUUID(), Instant.now(),payload , eventType, eventVersion );
    }
}
