package com.arcone.biopro.distribution.inventory.adapter.in.listener.received;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.AddQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductsReceivedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductReceivedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Mapper(componentModel = "spring")
public interface ProductsReceivedMessageMapper extends MessageMapper<ProductsReceivedInput, ProductsReceived> {

    @Mapping(target = "products", expression = "java(toProductReceivedInputList(message))")
    ProductsReceivedInput toInput(ProductsReceived message);

    default List<ProductReceivedInput> toProductReceivedInputList(ProductsReceived message) {
        return message.getProducts().stream().map(p -> this.toProductReceivedInput(message, p))
            .toList();
    }

    @Mapping(target = "unitNumber", source = "product.unitNumber")
    @Mapping(target = "productCode", source = "product.productCode")
    @Mapping(target = "inventoryLocation", source = "message.locationCode")
    @Mapping(target = "quarantines", source = "product.consequences")
    ProductReceivedInput toProductReceivedInput(ProductsReceived message, ProductsReceived.ReceivedProduct product);

    default String map(List<ProductsReceived.ReceivedConsequence> consequences) {
        if (consequences == null || consequences.isEmpty()) {
            return "";
        }
        return consequences.stream()
            .filter(c -> "QUARANTINE".equals(c.getConsequenceType()))
            .flatMap(consequence -> consequence.getConsequenceReasons().stream())
            .reduce((r1, r2) -> r1 + "," + r2)
            .orElse("");
    }

    default List<AddQuarantineInput> toAddQuarantineInput(List<ProductsReceived.ReceivedConsequence> consequences) {
        if (consequences == null) {
            return null;
        }

        AtomicLong id = new AtomicLong(0);
        return consequences.stream()
            .filter(c -> "QUARANTINE".equals(c.getConsequenceType()))
            .flatMap(consequence -> consequence.getConsequenceReasons().stream())
            .map(r -> AddQuarantineInput.builder().quarantineId(id.incrementAndGet()).reason(r).build())
            .toList();
    }
}