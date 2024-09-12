package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface LabelAppliedMessageMapper {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Mapping(target = "shortDescription", source = "productDescription")
    InventoryInput toInput(LabelAppliedMessage message);

    default  LocalDateTime toLocalDateTime(String dateString) {
        if(dateString == null) {
            return null;
        }

        String modifiedDateString = dateString.replace("Z", "");
        return LocalDateTime.parse(modifiedDateString, formatter);
    }
}
