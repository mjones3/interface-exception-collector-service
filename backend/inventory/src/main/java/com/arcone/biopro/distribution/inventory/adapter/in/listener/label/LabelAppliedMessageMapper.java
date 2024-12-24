package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface LabelAppliedMessageMapper extends MessageMapper<InventoryInput, LabelAppliedMessage> {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Mapping(target = "shortDescription", source = "productDescription")
    InventoryInput toInput(LabelAppliedMessage message);

    default  LocalDateTime toLocalDateTime(String dateTimeString) {
        if(dateTimeString == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);

        return zonedDateTime.toLocalDateTime();
    }
}
