package com.arcone.biopro.distribution.inventory.adapter.in.listener.imported;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.AddQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductsImportedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Mapper(componentModel = "spring")
public interface ProductsImportedMessageMapper extends MessageMapper<ProductsImportedInput, ProductsImported> {


    @Mapping(target = "products", expression = "java(toProductCreatedInputList(message))")
    ProductsImportedInput toInput(ProductsImported message);

    default List<ProductCreatedInput> toProductCreatedInputList(ProductsImported message) {
        return message.getProducts().stream().map(p -> this.toProductCreatedInput(message, p))
            .toList();
    }

    @Mapping(target = "unitNumber", source = "product.unitNumber")
    @Mapping(target = "productCode", source = "product.productCode")
    @Mapping(target = "productDescription", source = "product.productDescription")
    @Mapping(target = "expirationDate", expression = "java(extractDate(product.getExpirationDate()))")
    @Mapping(target = "expirationTime", expression = "java(extractTime(product.getExpirationDate()))")
    @Mapping(target = "temperatureCategory", source = "message.temperatureCategory")
    @Mapping(target = "expirationTimeZone", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "collectionDate", ignore = true)
    @Mapping(target = "inventoryLocation", source = "message.locationCode")
    @Mapping(target = "collectionLocation", ignore = true)
    @Mapping(target = "collectionTimeZone", ignore = true)
    @Mapping(target = "productFamily", source = "product.productFamily")
    @Mapping(target = "aboRh", source = "product.aboRh")
    @Mapping(target = "inputProducts", ignore = true)
    @Mapping(target = "quarantines", source = "product.consequences")
    @Mapping(target = "licensed", expression = "java(getLicense(product))")
    ProductCreatedInput toProductCreatedInput(ProductsImported message, ProductsImported.ImportedProduct product);


    default List<AddQuarantineInput> toAddQuarantineInput(List<ProductsImported.ImportedConsequence> consequences) {
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

    default Boolean getLicense(ProductsImported.ImportedProduct product) {
        return Objects.nonNull(product.getProperties()) && "LICENSED".equals(product.getProperties().get("LICENSE_STATUS"));
    }

    default String extractDate(LocalDateTime date) {
        return date != null ? date
            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : null;
    }

    default String extractTime(LocalDateTime date) {
        return date != null ? date
            .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
    }


}
