package com.arcone.biopro.distribution.inventory.adapter.in.listener.checkin;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.CheckInCompletedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CheckInCompletedMessageMapper extends MessageMapper<CheckInCompletedInput, CheckInCompletedMessage> {

    @Mapping(target = "collectionDate", source = "drawTime")
    @Mapping(target = "location", source = "collectionLocation")
    CheckInCompletedInput toInput(CheckInCompletedMessage message);
}
